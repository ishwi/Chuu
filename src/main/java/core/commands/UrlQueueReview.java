package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Validator;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.ImageQueue;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.TriFunction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class UrlQueueReview extends ConcurrentCommand<CommandParameters> {
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private static final String ACCEPT = "U+2714";
    private static final String DELETE = "U+1f469U+200dU+2696U+fe0f";
    private static final String RIGHT_ARROW = "U+27a1";
    private final TriFunction<JDA, Integer, Supplier<Integer>, BiFunction<ImageQueue, EmbedBuilder, EmbedBuilder>> builder = (jda, totalCount, pos) -> (reportEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Artist:", String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(reportEntity.getArtistName()), CommandUtil.getLastFmArtistUrl(reportEntity.getArtistName())), false)
                    .addField("Author", CommandUtil.getGlobalUsername(jda, reportEntity.getUploader()), true)
                    .addField("#Times user got reported:", String.valueOf(reportEntity.getUserReportCount()), true)
                    .setFooter(String.format("%d/%d%nUse \uD83D\uDC69\u200D\u2696\ufe0f to reject this image", pos.get() + 1, totalCount))
                    .setImage(CommandUtil.noImageUrl(reportEntity.getUrl()))
                    .setColor(CommandUtil.randomColor());

    public UrlQueueReview(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
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
        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Image Queue Review");
        LocalDateTime localDateTime = LocalDateTime.now();
        Set<Long> skippedIds = new HashSet<>();
        try {
            int totalReports = getService().getQueueUrlCount();
            HashMap<String, BiFunction<ImageQueue, MessageReactionAddEvent, Boolean>> actionMap = new HashMap<>();
            actionMap.put(DELETE, (reportEntity, r) -> {
                getService().rejectQueuedImage(reportEntity.getQueuedId());
                statDeclined.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;

            });
            actionMap.put(ACCEPT, (a, r) -> {
                getService().acceptImageQueue(a.getQueuedId(), a.getUrl(), a.getArtistId(), a.getUploader());
                try {
                    LastFMData lastFMData1 = getService().findLastFMData(a.getUploader());
                    if (lastFMData1.isImageNotify()) {
                        r.getJDA().retrieveUserById(a.getUploader()).flatMap(User::openPrivateChannel).queue(x ->
                                x.sendMessage("Your image for " + a.getArtistName() + " has been approved:\n" +
                                        "You can disable this automated message with the config command.\n" + a.getUrl()).queue());
                    }
                } catch (InstanceNotFoundException ignored) {
                    // Do nothing
                }
                statAccepeted.getAndIncrement();
                navigationCounter.incrementAndGet();
                return false;
            });
            actionMap.put(RIGHT_ARROW, (a, r) -> {
                skippedIds.add(a.getQueuedId());
                navigationCounter.incrementAndGet();
                return false;
            });
            new Validator<>(
                    finalEmbed -> {
                        int reportCount = getService().getQueueUrlCount();
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
                                .setColor(CommandUtil.randomColor());
                    },
                    () -> getService().getNextQueue(localDateTime, skippedIds),
                    builder.apply(e.getJDA(), totalReports, navigationCounter::get)
                    , embedBuilder, e.getChannel(), e.getAuthor().getIdLong(), actionMap, false, true);
        } catch (Throwable ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        } finally {
            this.isActive.set(false);
        }

    }
}