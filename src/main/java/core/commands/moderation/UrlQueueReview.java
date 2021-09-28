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
import dao.ImageQueue;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static core.otherlisteners.Reactions.*;

public class UrlQueueReview extends ConcurrentCommand<CommandParameters> {

    private final HexaFunction<JDA, AtomicInteger, Supplier<Integer>, Map<Long, Integer>, Map<Long, Integer>, Map<Long, Integer>, BiFunction<ImageQueue, EmbedBuilder, EmbedBuilder>> builder = (jda, totalCount, pos, strikes, rejected, accepted) -> (reportEntity, embedBuilder) ->
            addStrikeField(reportEntity, strikes, embedBuilder.clearFields()
                    .addField("Artist:", String.format("[%s](%s)", CommandUtil.escapeMarkdown(reportEntity.artistName()), LinkUtils.getLastFmArtistUrl(reportEntity.artistName())), false)
                    .addField("Author", CommandUtil.getGlobalUsername(reportEntity.uploader()), true)
                    .addField("# Rejected:", String.valueOf(reportEntity.userRejectedCount() + rejected.getOrDefault(reportEntity.uploader(), 0)), true)
                    .addField("# Approved:", String.valueOf(reportEntity.count() + accepted.getOrDefault(reportEntity.uploader(), 0)), true)
                    .setFooter(String.format("%d/%d%nUse 👩🏾‍⚖️ to reject this image", pos.get() + 1, totalCount.get()))
                    .setImage(CommandUtil.noImageUrl(reportEntity.url()))
                    .setColor(CommandUtil.pastelColor()));

    public UrlQueueReview(ServiceView dao) {
        super(dao);
    }

    private static EmbedBuilder addStrikeField(ImageQueue q, Map<Long, Integer> strikesMap, EmbedBuilder embedBuilder) {
        int strikes = q.strikes() + strikesMap.getOrDefault(q.uploader(), 0);
        if (strikes != 0) {
            embedBuilder
                    .addField("# Strikes:", String.valueOf(q.strikes() + strikesMap.getOrDefault(q.uploader(), 0)), true);
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
    protected void onCommand(Context e, @Nonnull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can review the reported images!");
            return;
        }
        AtomicInteger statDeclined = new AtomicInteger(0);
        AtomicInteger navigationCounter = new AtomicInteger(0);
        AtomicInteger statAccepeted = new AtomicInteger(0);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setTitle("Image queue review");
        Map<Long, Integer> strikesMap = new HashMap<>();
        Map<Long, Integer> rejectedMap = new HashMap<>();
        Map<Long, Integer> approvedMap = new HashMap<>();
        Queue<ImageQueue> queue = new ArrayDeque<>(db.getNextQueue());
        AtomicInteger totalImages = new AtomicInteger(queue.size());
        HashMap<String, Reaction<ImageQueue, ButtonClickEvent, ButtonResult>> actionMap = new LinkedHashMap<>();
        actionMap.put(DELETE, (q, r) -> {
            rejectedMap.merge(q.uploader(), 1, Integer::sum);
            db.rejectQueuedImage(q.queuedId(), q);
            statDeclined.getAndIncrement();
            navigationCounter.incrementAndGet();
            return ButtonResult.defaultResponse;

        });
        actionMap.put(RIGHT_ARROW, (a, r) -> {
            navigationCounter.incrementAndGet();
            return ButtonResult.defaultResponse;
        });
        actionMap.put(ACCEPT, (a, r) -> {
            approvedMap.merge(a.uploader(), 1, Integer::sum);

            statAccepeted.getAndIncrement();
            navigationCounter.incrementAndGet();
            CompletableFuture.runAsync(() -> {
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
            });
            return ButtonResult.defaultResponse;
        });

        actionMap.put(STRIKE, (a, r) -> {
            CompletableFuture.runAsync(() -> {
                boolean banned = db.strikeQueue(a.queuedId(), a);
                strikesMap.merge(a.uploader(), 1, Integer::sum);
                if (banned) {
                    TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channel2Id);
                    db.removeQueuedPictures(a.uploader());
                    queue.removeIf(qI -> qI.uploader() == a.uploader());
                    totalImages.set(queue.size());
                    if (textChannelById != null)
                        textChannelById.sendMessageEmbeds(new ChuuEmbedBuilder(e).setTitle("Banned user for adding pics")
                                .setDescription("User: **%s**\n".formatted(User.fromId(a.uploader()).getAsMention())).build()).queue();
                }
            });
            statDeclined.getAndIncrement();
            navigationCounter.incrementAndGet();
            return ButtonResult.defaultResponse;
        });

        ActionRow of = ActionRow.of(
                Button.danger(DELETE, "Deny").withEmoji(Emoji.fromUnicode(DELETE)),
                Button.primary(ACCEPT, "Accept").withEmoji(Emoji.fromUnicode(ACCEPT)),
                Button.secondary(RIGHT_ARROW, "Skip").withEmoji(Emoji.fromUnicode(RIGHT_ARROW)),
                Button.danger(STRIKE, "Strike").withEmoji(Emoji.fromUnicode(STRIKE))
        );
        new ButtonValidator<>(
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
                    } else if (navigationCounter.get() == totalImages.get()) {
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
                queue::poll,
                builder.apply(e.getJDA(), totalImages, navigationCounter::get, strikesMap, rejectedMap, approvedMap)
                , embedBuilder, e, e.getAuthor().getIdLong(), actionMap, List.of(of), false, true, 60);

    }

    private interface HexaFunction<A, B, C, D, E, F, G> {
        G apply(A a, B b, C c, D d, E e, F f);
    }

}
