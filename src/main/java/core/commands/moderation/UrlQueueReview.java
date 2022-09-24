package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.ButtonResult;
import core.otherlisteners.ButtonValidatorBuilder;
import core.otherlisteners.Reaction;
import core.otherlisteners.util.Response;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import core.util.ServiceView;
import dao.ImageQueue;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nonnull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
        return new NoOpParser().addOptional(new OptionalEntity("new", "new"));
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
    public void onCommand(Context e, @Nonnull CommandParameters params) throws InstanceNotFoundException {
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
        Queue<ImageQueue> queue = new ArrayDeque<>(db.getNextQueue(params.hasOptional("new")));
        AtomicInteger totalImages = new AtomicInteger(queue.size());


        ActionRow mainRow = ActionRow.of(
                Button.danger(DELETE, "Deny").withEmoji(Emoji.fromUnicode(DELETE)),
                Button.primary(ACCEPT, "Accept").withEmoji(Emoji.fromUnicode(ACCEPT)),
                Button.secondary(RIGHT_ARROW, "Skip").withEmoji(Emoji.fromUnicode(RIGHT_ARROW)),
                Button.danger(STRIKE, "Strike").withEmoji(Emoji.fromUnicode(STRIKE))
        );

        AtomicReference<ImageQueue> previousItem = new AtomicReference<>(queue.peek());


        Supplier<ActionRow> reverseSearchRow = () -> {
            ImageQueue peek = previousItem.get();
            if (peek != null) {
                String encoded = URLEncoder.encode(peek.url(), StandardCharsets.UTF_8);
                return ActionRow.of(
                        Button.link("https://yandex.com/images/search?rpt=imageview&url=" + encoded, "Yandex"),
                        Button.link("https://www.google.com/searchbyimage?&image_url=" + encoded, "Google")
                );

            }
            return null;
        };


        ButtonResult buttonResponse = () -> {
            ActionRow secondRow = reverseSearchRow.get();
            if (secondRow != null) {
                return new ButtonResult.Result(false, List.of(mainRow, secondRow));
            }
            return Response.def;
        };

        HashMap<String, Reaction<ImageQueue, ButtonInteractionEvent, ButtonResult>> actionMap = new LinkedHashMap<>();
        actionMap.put(DELETE, (q, r) -> {
            rejectedMap.merge(q.uploader(), 1, Integer::sum);
            db.rejectQueuedImage(q.queuedId(), q);
            statDeclined.getAndIncrement();
            navigationCounter.incrementAndGet();
            return buttonResponse;
        });

        actionMap.put(RIGHT_ARROW, (a, r) -> {
            navigationCounter.incrementAndGet();
            return buttonResponse;
        });
        actionMap.put(ACCEPT, (a, r) -> {
            approvedMap.merge(a.uploader(), 1, Integer::sum);

            statAccepeted.getAndIncrement();
            navigationCounter.incrementAndGet();
            CommandUtil.runLog(() -> {
                long id = db.acceptImageQueue(a.queuedId(), a.url(), a.artistId(), a.uploader());
                if (a.guildId() != null) {
                    db.insertServerCustomUrl(id, a.guildId(), a.artistId());
                    r.getJDA().retrieveUserById(a.uploader()).flatMap(User::openPrivateChannel).queue(x ->
                            x.sendMessage("Your image for " + a.artistName() + " has been approved and has been set as the default image on your server.").queue());
                } else {
                    try {
                        LastFMData lastFMData1 = db.findLastFMData(a.uploader());
                        if (lastFMData1.isImageNotify()) {
                            r.getJDA().retrieveUserById(a.uploader()).flatMap(User::openPrivateChannel).queue(x ->
                                    x.sendMessage("Your image for " + a.artistName() + " has been approved:\n" +
                                                  "You can disable this automated message with the config command.\n" + a.url()).queue());
                        }
                    } catch (InstanceNotFoundException ignored) {
                        // Do nothing
                    }
                }
            });
            return buttonResponse;
        });

        actionMap.put(STRIKE, (a, r) -> {
            strikesMap.merge(a.uploader(), 1, Integer::sum);
            CommandUtil.runLog(() -> {
                boolean banned = db.strikeQueue(a.queuedId(), a);
                if (banned) {
                    db.removeQueuedPictures(a.uploader());
                    queue.removeIf(qI -> qI.uploader() == a.uploader());
                    totalImages.set(queue.size());

                    TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channel2Id);
                    if (textChannelById != null)
                        textChannelById.sendMessageEmbeds(new ChuuEmbedBuilder(e).setTitle("Banned user for adding pics")
                                .setDescription("User: **%s**\n".formatted(User.fromId(a.uploader()).getAsMention())).build()).queue();
                }
            });
            statDeclined.getAndIncrement();
            navigationCounter.incrementAndGet();
            return buttonResponse;
        });


        List<ActionRow> initialList;

        ActionRow secondRow = reverseSearchRow.get();
        if (secondRow != null) {
            initialList = List.of(mainRow, secondRow);
        } else {
            initialList = List.of(mainRow);
        }

        UnaryOperator<EmbedBuilder> finishingView = prev -> {
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
                title = "Timed out";
            }
            return prev.setTitle(title)
                    .setImage(null)
                    .clearFields()
                    .setDescription(description)
                    .setFooter(String.format("There are %d %s left to review", reportCount, CommandUtil.singlePlural(reportCount, "image", "images")))
                    .setColor(CommandUtil.pastelColor());
        };

        new ButtonValidatorBuilder<ImageQueue>()
                .setGetLastMessage(finishingView)
                .setElementFetcher(() -> {
                    ImageQueue nextInQ = queue.poll();
                    previousItem.set(nextInQ);
                    return nextInQ;
                }).setFillBuilder(builder.apply(e.getJDA(), totalImages, navigationCounter::get, strikesMap, rejectedMap, approvedMap))
                .setWho(embedBuilder)
                .setContext(e)
                .setDiscordId(e.getAuthor().getIdLong())
                .setActionMap(actionMap)
                .setActionRows(initialList)
                .setAllowOtherUsers(false)
                .setRenderInSameElement(true)
                .setChannelId(e.getChannel().getIdLong())
                .setActiveSeconds(90)
                .queue();

    }

    private interface HexaFunction<A, B, C, D, E, F, G> {
        G apply(A a, B b, C c, D d, E e, F f);
    }

}
