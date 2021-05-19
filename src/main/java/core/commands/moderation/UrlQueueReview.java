package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Validator;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ImageQueue;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.TriFunction;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class UrlQueueReview extends ConcurrentCommand<CommandParameters> {
    private static final String ACCEPT = "U+2714";
    private static final String DELETE = "U+1f469U+200dU+2696U+fe0f";
    private static final String RIGHT_ARROW = "U+27a1";
    private static final String STRIKE = "U+1f3f3U+fe0f";
    private final AtomicBoolean isActive = new AtomicBoolean(false);


    private final TriFunction<JDA, Integer, Supplier<Integer>, BiFunction<ImageQueue, EmbedBuilder, EmbedBuilder>> builder = (jda, totalCount, pos) -> (reportEntity, embedBuilder) ->
            addStrikeField(reportEntity, embedBuilder.clearFields()
                    .addField("Artist:", String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(reportEntity.artistName()), LinkUtils.getLastFmArtistUrl(reportEntity.artistName())), false)
                    .addField("Author", CommandUtil.getGlobalUsername(jda, reportEntity.uploader()), true)
                    .addField("# Rejected:", String.valueOf(reportEntity.userRejectedCount()), true)
                    .addField("# Approved:", String.valueOf(reportEntity.count()), true)
                    .setFooter(String.format("%d/%d%nUse \uD83D\uDC69\u200D\u2696\ufe0f to reject this image", pos.get() + 1, totalCount))
                    .setImage(CommandUtil.noImageUrl(reportEntity.url()))
                    .setColor(CommandUtil.pastelColor()));

    public UrlQueueReview(ServiceView dao) {
        super(dao);
    }

    private static EmbedBuilder addStrikeField(ImageQueue q, EmbedBuilder embedBuilder) {
        int strikes = q.strikes();
        if (strikes != 0) {
            embedBuilder
                    .addField("# Strikes:", String.valueOf(q.strikes()), true);
        }
        if (q.guildId() != null) {
            embedBuilder.addField("For guild*", "", true);
        }
        return embedBuilder;
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
        return "Image Review";
    }

    @Override
    public List<String> getAliases() {
        return List.of("review");
    }

    @Override
    public String getName() {
        return "Image Review";
    }

    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can review the reported images!");
            return;
        }
        if (!this.isActive.compareAndSet(false, true)) {
            sendMessageQueue(e, "Other admin is reviewing the image queue, pls wait till they have finished!");
            return;
        }
        AtomicInteger statDeclined = new AtomicInteger(0);
        AtomicInteger navigationCounter = new AtomicInteger(0);
        AtomicInteger statAccepeted = new AtomicInteger(0);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setTitle("Image Queue Review");
        long maxId;
        ImageQueue nextQueue = db.getNextQueue(Long.MAX_VALUE, new HashSet<>());
        if (nextQueue == null) {
            maxId = Long.MAX_VALUE;
        } else {
            maxId = nextQueue.queuedId();
        }
        Set<Long> skippedIds = new HashSet<>();
        try {
            int totalReports = db.getQueueUrlCount();
            HashMap<String, BiFunction<ImageQueue, MessageReactionAddEvent, Boolean>> actionMap = new LinkedHashMap<>();
            actionMap.put(DELETE, (reportEntity, r) -> {
                db.rejectQueuedImage(reportEntity.queuedId(), reportEntity);
                statDeclined.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;

            });
            actionMap.put(RIGHT_ARROW, (a, r) -> {
                skippedIds.add(a.queuedId());
                navigationCounter.incrementAndGet();
                return false;
            });
            actionMap.put(ACCEPT, (a, r) -> {
                long id = db.acceptImageQueue(a.queuedId(), a.url(), a.artistId(), a.uploader());
                if (a.guildId() != null) {
                    db.insertServerCustomUrl(id, a.guildId(), a.artistId());
                    r.getJDA().retrieveUserById(a.uploader(), false).flatMap(User::openPrivateChannel).queue(x ->
                            x.sendMessage("Your image for " + a.artistName() + " has been approved and has been set as the default image on your server.").queue());
                } else {
                    try {
                        LastFMData lastFMData1 = db.findLastFMData(a.uploader());
                        if (lastFMData1.isImageNotify()) {
                            r.getJDA().retrieveUserById(a.uploader(), false).flatMap(User::openPrivateChannel).queue(x ->
                                    x.sendMessage("Your image for " + a.artistName() + " has been approved:\n" +
                                                  "You can disable this automated message with the config command.\n" + a.url()).queue());
                        }
                    } catch (InstanceNotFoundException ignored) {
                        // Do nothing
                    }
                }
                statAccepeted.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;
            });

            actionMap.put(STRIKE, (a, r) -> {
                boolean banned = db.strikeQueue(a.queuedId(), a);
                if (banned) {
                    TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channel2Id);
                    if (textChannelById != null)
                        textChannelById.sendMessage(new ChuuEmbedBuilder(e).setTitle("Banned user for adding pics")
                                .setDescription("User: **%s**\n".formatted(User.fromId(a.uploader()).getAsMention())).build()).queue();
                }
                statDeclined.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;
            });
            new Validator<>(
                    finalEmbed -> {
                        int reportCount = db.getQueueUrlCount();
                        String description = (navigationCounter.get() == 0) ? null :
                                             String.format("You have seen %d %s and decided to reject %d %s and to accept %d",
                                                     navigationCounter.get(),
                                                     CommandUtil.singlePlural(navigationCounter.get(), "image", "images"),
                                                     statDeclined.get(),
                                                     CommandUtil.singlePlural(statDeclined.get(), "image", "images"),
                                                     statAccepeted.get());
                        String title;
                        if (navigationCounter.get() == 0) {
                            title = "There are no images in the queue";
                        } else if (navigationCounter.get() == totalReports) {
                            title = "There are no more images in the queue";
                        } else {
                            title = "Timed Out";
                        }
                        return finalEmbed.setTitle(title)
                                .setImage(null)
                                .clearFields()
                                .setDescription(description)
                                .setFooter(String.format("There are %d %s left to review", reportCount, CommandUtil.singlePlural(reportCount, "image", "images")))
                                .setColor(CommandUtil.pastelColor());
                    },
                    () -> db.getNextQueue(maxId, skippedIds),
                    builder.apply(e.getJDA(), totalReports, navigationCounter::get)
                    , embedBuilder, e, e.getAuthor().getIdLong(), actionMap, false, true);
        } catch (Throwable ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        } finally {
            this.isActive.set(false);
        }

    }
}
