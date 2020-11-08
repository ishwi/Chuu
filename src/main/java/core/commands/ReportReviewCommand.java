package core.commands;

import core.Chuu;
import core.exceptions.LastFmException;
import core.otherlisteners.Validator;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ReportEntity;
import dao.entities.Role;
import dao.entities.TriFunction;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ReportReviewCommand extends ConcurrentCommand<CommandParameters> {
    private static final String ACCEPT = "U+2714";
    private static final String DELETE = "U+1f469U+200dU+2696U+fe0f";
    private static final String RIGHT_ARROW = "U+27a1";
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private final TriFunction<JDA, Integer, Supplier<Integer>, BiFunction<ReportEntity, EmbedBuilder, EmbedBuilder>> builder = (jda, integer, pos) -> (reportEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Author", CommandUtil.getGlobalUsername(jda, reportEntity.getWhoGotReported()), true)
                    .addField("#Times user got reported:", String.valueOf(reportEntity.getUserTotalReports()), true)
                    .addField("Image score:", String.valueOf(reportEntity.getCurrentScore()), false)
                    .addField("Number of reports on this image:", String.valueOf(reportEntity.getReportCount()), true)
                    .addField("Artist:", String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(reportEntity.getArtistName()), LinkUtils.getLastFmArtistUrl(reportEntity.getArtistName())), false)
                    .setFooter(String.format("%d/%d%nUse \uD83D\uDC69\u200D\u2696\ufe0f to remove this image", pos.get() + 1, integer))
                    .setImage(CommandUtil.noImageUrl(reportEntity.getUrl()))
                    .setColor(CommandUtil.randomColor());

    public ReportReviewCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
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
    void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
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
        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Reports Review");
        // TODO :DD
        long maxId;
        ReportEntity nextReport = getService().getNextReport(Long.MAX_VALUE, new HashSet<>());
        if (nextReport == null) {
            maxId = Long.MAX_VALUE;
        } else {
            maxId = nextReport.getReportId();
        }
        Set<Long> skippedIds = new HashSet<>();
        try {
            int totalReports = getService().getReportCount();
            HashMap<String, BiFunction<ReportEntity, MessageReactionAddEvent, Boolean>> actionMap = new HashMap<>();
            actionMap.put(DELETE, (reportEntity, r) -> {
                getService().removeReportedImage(reportEntity.getImageReported(), reportEntity.getWhoGotReported(), idLong);
                statBan.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;

            });
            actionMap.put(ACCEPT, (a, r) -> {
                getService().ignoreReportedImage(a.getImageReported());
                statIgnore.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;
            });
            actionMap.put(RIGHT_ARROW, (a, r) -> {
                skippedIds.add(a.getImageReported());
                navigationCounter.incrementAndGet();
                return false;
            });


            new Validator<>(
                    finalEmbed -> {
                        int reportCount = getService().getReportCount();
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
                                .setColor(CommandUtil.randomColor());
                    },
                    () -> getService().getNextReport(maxId, skippedIds),
                    builder.apply(e.getJDA(), totalReports, navigationCounter::get)
                    , embedBuilder, e.getChannel(), e.getAuthor().getIdLong(), actionMap, false, true);
        } catch (Throwable ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        } finally {
            this.isActive.set(false);
        }

    }
}
