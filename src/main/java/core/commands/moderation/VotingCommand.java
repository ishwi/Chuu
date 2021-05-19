package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Validator;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.ColorService;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.validation.constraints.NotNull;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class VotingCommand extends ConcurrentCommand<ArtistParameters> {
    private static final String RIGHT_ARROW = "U+27a1";
    private static final String LEFT_ARROW = "U+2b05";
    private static final String UP_VOTE = "U+1f44d";
    private static final String DOWN_VOTE = "U+1f44e";
    private static final String REPORT = "U+1f6ab";
    private static final String CANCEL = "U+1f3f3U+fe0f";


    private final TriFunction<JDA, AtomicInteger, AtomicInteger, BiFunction<VotingEntity, EmbedBuilder, EmbedBuilder>> builder = (jda, size, counter) -> (votingEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Added by", CommandUtil.getGlobalUsername(jda, votingEntity.getOwner()), true)
                    .addBlankField(true)
                    .addField("Submitted:", votingEntity.getDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy MM dd HH:mm ")), true)
                    .addField("Points:", String.valueOf(votingEntity.getVotes()), true)
                    .addBlankField(true)
                    .addField("Vote Count:", String.valueOf(votingEntity.getTotalVotes()), true)
                    .setFooter("%d/%d".formatted(counter.get() + 1, size.get()) + "\nUse \uD83D\uDEAB to report this image")
                    .setImage(CommandUtil.noImageUrl(votingEntity.getUrl()))
                    .setColor(CommandUtil.pastelColor());

    public VotingCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.ARTIST_IMAGES;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Vote for an image. The top voted image will be displayed in the bot commands";
    }

    @Override
    public List<String> getAliases() {
        return List.of("vote", "voting", "images", "image", "v");
    }

    @Override
    public String getName() {
        return "Vote";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException, InstanceNotFoundException {


        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (lastFMData.getRole() == Role.IMAGE_BLOCKED) {
            sendMessageQueue(e, "You don't have enough permissions to vote for images");
            return;
        }
        String preCorrectionArtist = params.getArtist();
        ScrobbledArtist artist = CommandUtil.onlyCorrection(db, preCorrectionArtist, lastFM, !params.isNoredirect());
        List<VotingEntity> allArtistImages = db.getAllArtistImages(artist.getArtistId());
        if (allArtistImages.isEmpty()) {
            sendMessageQueue(e, artist.getArtist() + " doesn't have any image");
            return;
        }
        String correctedArtist = CommandUtil.cleanMarkdownCharacter(allArtistImages.get(0).getArtist());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle(correctedArtist + " Images");

        AtomicInteger counter = new AtomicInteger(0);
        Map<String, BiFunction<VotingEntity, MessageReactionAddEvent, Boolean>> actionMap = new LinkedHashMap<>();
        List<Long> guildList = e.isFromGuild()
                               ? db.getAll(e.getGuild().getIdLong()).stream().filter(u -> !u.getRole().equals(Role.IMAGE_BLOCKED)).map(UsersWrapper::getDiscordID).toList()
                               : List.of(e.getAuthor().getIdLong());


        actionMap.put(UP_VOTE, (a, r) -> {
            if (guildList.contains(r.getUserIdLong())) {
                VoteStatus voteStatus = db.castVote(a.getUrlId(), r.getUserIdLong(), true);
                if (voteStatus.equals(VoteStatus.CHANGE_VALUE)) {
                    a.changeToAPositive();
                } else if (voteStatus.equals(VoteStatus.NEW_VOTE)) {
                    a.add();
                    a.incrementTotalVotes();
                }
            }
            return false;
        });
        actionMap.put(DOWN_VOTE, (a, r) -> {
            if (guildList.contains(r.getUserIdLong())) {
                VoteStatus voteStatus = db.castVote(a.getUrlId(), r.getUserIdLong(), false);
                if (voteStatus.equals(VoteStatus.CHANGE_VALUE)) {
                    a.changeToANegative();
                } else if (voteStatus.equals(VoteStatus.NEW_VOTE)) {
                    a.decrement();
                    a.incrementTotalVotes();
                }
            }
            return false;
        });

        if (allArtistImages.size() > 1) {
            actionMap.put(LEFT_ARROW, (aliasEntity, r) -> leftMove(allArtistImages, counter, r, true));
            actionMap.put(RIGHT_ARROW, (a, r) -> rightMove(allArtistImages, counter, r, true));
        }
        actionMap.put(REPORT, (a, r) -> {
            if (guildList.contains(r.getUserIdLong())) {
                db.report(a.getUrlId(), r.getUserIdLong());
            }
            return false;
        });
        AtomicInteger size = new AtomicInteger(allArtistImages.size());

        if (lastFMData.getRole() == Role.ADMIN) {
            AtomicInteger deletedCounter = new AtomicInteger();
            actionMap.put(CANCEL, (a, r) -> {
                if (r.getUserIdLong() == e.getAuthor().getIdLong()) {
                    db.removeReportedImage(a.getUrlId(), a.getOwner(), r.getUserIdLong());
                    counter.decrementAndGet();
                    allArtistImages.removeIf(ve -> ve.getUrlId() == a.getUrlId());
                    size.decrementAndGet();
                }
                int i = allArtistImages.size() - 1;
                if (counter.get() == i && allArtistImages.size() > 0)
                    rightMove(allArtistImages, counter, r, false);
                else if (counter.get() < i) {
                    leftMove(allArtistImages, counter, r, false);
                }  // Do nothing
                return false;
            });
        }
        new Validator<>(
                finalEmbed -> {
                    VotingEntity first = allArtistImages.stream().max(Comparator.comparingLong(VotingEntity::getVotes)).orElse(allArtistImages.isEmpty() ? null : allArtistImages.get(0));
                    String description;
                    String title;
                    if (first == null) {
                        title = "All images deleted";
                        description = "This artist no longer has any image";
                    } else {
                        title = "Voting timed out";
                        description = "Submitted by: " + CommandUtil.getGlobalUsername(e.getJDA(), first.getOwner()) + "\n\n";
                        if (first != allArtistImages.get(0)) {
                            description += "The artist image for " + CommandUtil.cleanMarkdownCharacter(first.getArtist()) + " has changed to:";
                        } else {
                            description += "The top voted image for " + CommandUtil.cleanMarkdownCharacter(first.getArtist()) + " is:";
                        }
                    }
                    finalEmbed.setTitle(title)
                            .setImage(first != null ? first.getUrl() : null)
                            .clearFields()
                            .setDescription(description);
                    if (first != null) {
                        finalEmbed.setFooter(String.format("Has %d %s with %d%s", first.getVotes(), CommandUtil.singlePlural(first.getVotes(), "point", "points"), first.getTotalVotes(),
                                CommandUtil.singlePlural(first.getTotalVotes(), " vote", " votes")))
                                .setColor(ColorService.computeColor(e));
                    } else {
                        finalEmbed.setFooter(null);
                    }
                    return finalEmbed;
                },
                () -> {
                    if (counter.get() >= allArtistImages.size() - 1) {
                        counter.set(allArtistImages.size() - 1);
                    }
                    if (counter.get() < 0) {
                        counter.set(0);
                    }
                    if (allArtistImages.isEmpty()) {
                        return null;
                    }
                    return allArtistImages.get(counter.get());
                },
                builder.apply(e.getJDA(), size, counter)
                , embedBuilder, e, e.getAuthor().getIdLong(), actionMap, true, true);

    }

    @org.jetbrains.annotations.NotNull
    private Boolean leftMove(List<VotingEntity> allArtistImages, AtomicInteger counter, MessageReactionAddEvent r, boolean isSame) {
        int i = counter.decrementAndGet();
        if (i == 0) {
            if (isSame) {
                r.getReaction().clearReactions().queue();
            } else {
                r.getChannel().removeReactionById(r.getMessageId(), LEFT_ARROW).queue();
            }
        }
        if (i == allArtistImages.size() - 2) {
            r.getChannel().addReactionById(r.getMessageIdLong(), RIGHT_ARROW).queue();
        }
        return false;
    }

    @org.jetbrains.annotations.NotNull
    private Boolean rightMove(List<VotingEntity> allArtistImages, AtomicInteger counter, MessageReactionAddEvent r, boolean isSame) {
        int i = counter.incrementAndGet();
        if (i == allArtistImages.size() - 1) {
            if (isSame) {
                r.getReaction().clearReactions().queue();
            } else {
                r.getChannel().removeReactionById(r.getMessageId(), RIGHT_ARROW).queue();
            }
        }
        if (i == 1) {
            r.getChannel().addReactionById(r.getMessageIdLong(), LEFT_ARROW).queue();
        }
        return false;
    }
}

