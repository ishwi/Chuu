package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.ButtonResult;
import core.otherlisteners.ButtonValidator;
import core.otherlisteners.Reaction;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.ColorService;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ReportEntity;
import dao.entities.Role;
import dao.entities.TriFunction;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static core.otherlisteners.Reactions.*;

public class ReportReviewCommand extends ConcurrentCommand<CommandParameters> {


    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private final TriFunction<JDA, Integer, Supplier<Integer>, BiFunction<ReportEntity, EmbedBuilder, EmbedBuilder>> builder = (jda, integer, pos) -> (reportEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Author", CommandUtil.getGlobalUsername(reportEntity.getWhoGotReported()), true)
                    .addField("#Times user got reported:", String.valueOf(reportEntity.getUserTotalReports()), true)
                    .addField("Image score:", String.valueOf(reportEntity.getCurrentScore()), false)
                    .addField("Number of reports on this image:", String.valueOf(reportEntity.getReportCount()), true)
                    .addField("Artist:", String.format("[%s](%s)", CommandUtil.escapeMarkdown(reportEntity.getArtistName()), LinkUtils.getLastFmArtistUrl(reportEntity.getArtistName())), false)
                    .setFooter(String.format("%d/%d%nUse 👩🏾‍⚖️ to remove this image", pos.get() + 1, integer))
                    .setImage(CommandUtil.noImageUrl(reportEntity.getUrl()))
                    .setColor(CommandUtil.pastelColor());

    public ReportReviewCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Report Review";
    }

    @Override
    public List<String> getAliases() {
        return List.of("reports");
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can review the reported images!");
            return;
        }
        if (!this.isActive.compareAndSet(false, true)) {
            sendMessageQueue(e, "Other admin is reviewing the reported images, pls wait till they have finished!");
            return;
        }
        AtomicInteger statBan = new AtomicInteger(0);
        AtomicInteger navigationCounter = new AtomicInteger(0);
        AtomicInteger statIgnore = new AtomicInteger(0);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setTitle("Reports Review");
        // TODO :DD
        long maxId;
        ReportEntity nextReport = db.getNextReport(Long.MAX_VALUE, new HashSet<>());
        if (nextReport == null) {
            maxId = Long.MAX_VALUE;
        } else {
            maxId = nextReport.getReportId();
        }
        Set<Long> skippedIds = new HashSet<>();
        try {
            int totalReports = db.getReportCount();
            HashMap<String, Reaction<ReportEntity, ButtonInteractionEvent, ButtonResult>> actionMap = new HashMap<>();
            actionMap.put(DELETE, (reportEntity, r) -> {
                db.removeReportedImage(reportEntity.getImageReported(), reportEntity.getWhoGotReported(), idLong);
                statBan.getAndIncrement();
                navigationCounter.incrementAndGet();
                return ButtonResult.defaultResponse;

            });
            actionMap.put(ACCEPT, (a, r) -> {
                db.ignoreReportedImage(a.getImageReported());
                statIgnore.getAndIncrement();
                navigationCounter.incrementAndGet();
                return ButtonResult.defaultResponse;
            });
            actionMap.put(RIGHT_ARROW, (a, r) -> {
                skippedIds.add(a.getImageReported());
                navigationCounter.incrementAndGet();
                return ButtonResult.defaultResponse;
            });
            actionMap.put(STRIKE, (a, r) -> {
                statBan.getAndIncrement();
                navigationCounter.incrementAndGet();
                CompletableFuture.runAsync(() -> {
                    boolean banned = db.strikeExisting(a);
                    if (banned) {
                        TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channel2Id);
                        db.removeQueuedPictures(a.getWhoGotReported());
                        if (textChannelById != null)
                            textChannelById.sendMessageEmbeds(new ChuuEmbedBuilder(e).setTitle("Banned user for adding pics")
                                    .setDescription("User: **%s**\n".formatted(User.fromId(a.getWhoGotReported()).getAsMention())).build()).queue();
                    }
                });
                return ButtonResult.defaultResponse;
            });

            ActionRow of = ActionRow.of(
                    Button.danger(DELETE, "Remove image").withEmoji(Emoji.fromUnicode(DELETE)),
                    Button.primary(ACCEPT, "Ignore report").withEmoji(Emoji.fromUnicode(ACCEPT)),
                    Button.secondary(RIGHT_ARROW, "Skip").withEmoji(Emoji.fromUnicode(RIGHT_ARROW)),
                    Button.danger(STRIKE, "Strike").withEmoji(Emoji.fromUnicode(STRIKE))
            );

            new ButtonValidator<>(
                    finalEmbed -> {
                        int reportCount = db.getReportCount();
                        String description = (navigationCounter.get() == 0) ? null : String.format("You have seen %d %s and decided to delete %d %s and to ignore %d", navigationCounter.get(), CommandUtil.singlePlural(navigationCounter.get(), "image", "images"), statBan.get(), CommandUtil.singlePlural(statBan.get(), "image", "images"), statIgnore.get());
                        String title;
                        if (navigationCounter.get() == 0) {
                            title = "There are no reported images";
                        } else if (navigationCounter.get() == totalReports) {
                            title = "There are no more reported images";
                        } else {
                            title = "Timed Out";
                        }
                        return finalEmbed.setTitle(title)
                                .setImage(null)
                                .clearFields()
                                .setDescription(description)
                                .setFooter(String.format("There are %d %s left to review", reportCount, CommandUtil.singlePlural(reportCount, "image", "images")))
                                .setColor(ColorService.computeColor(e));
                    },
                    () -> db.getNextReport(maxId, skippedIds),
                    builder.apply(e.getJDA(), totalReports, navigationCounter::get)
                    , embedBuilder, e, e.getAuthor().getIdLong(), actionMap, List.of(of), false, true, 120);
        } catch (Throwable ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        } finally {
            this.isActive.set(false);
        }

    }
}
