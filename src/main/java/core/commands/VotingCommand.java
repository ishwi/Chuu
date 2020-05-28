package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.ReactionResponse;
import core.otherlisteners.Validator;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class VotingCommand extends ConcurrentCommand<ArtistParameters> {
    private static final String RIGHT_ARROW = "U+27a1";
    private static final String LEFT_ARROW = "U+2b05";
    private static final String UP_VOTE = "U+1f44d";
    private static final String DOWN_VOTE = "U+1f44e";
    private static final String REPORT = "U+1f6ab";


    private final BiFunction<JDA, Integer, BiFunction<VotingEntity, EmbedBuilder, EmbedBuilder>> builder = (jda, integer) -> (votingEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .addField("Added by", CommandUtil.getGlobalUsername(jda, votingEntity.getOwner()), true)
                    .addBlankField(true)
                    .addField("Submitted:", votingEntity.getDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy MM dd HH:mm ")), true)
                    .addField("Points:", String.valueOf(votingEntity.getVotes()), true)
                    .addBlankField(true)
                    .addField("Vote Count:", String.valueOf(votingEntity.getTotalVotes()), true)
                    .setFooter("Image Count: " + integer + " \nUse \uD83D\uDEAB to report this image")
                    .setImage(CommandUtil.noImageUrl(votingEntity.getUrl()))
                    .setColor(CommandUtil.randomColor());

    public VotingCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.ARTIST_IMAGES;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        return new ArtistParser(getService(), lastFM);
    }

    @Override
    public String getDescription() {
        return "Vote for an image. The top voted image will be displayed in the bot commands";
    }

    @Override
    public List<String> getAliases() {
        return List.of("vote", "voting", "images", "image");
    }

    @Override
    public String getName() {
        return "Vote";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters params = parser.parse(e);
        if (params == null) {
            return;
        }
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        if (lastFMData.getRole() == Role.IMAGE_BLOCKED) {
            sendMessageQueue(e, "You don't have enough permissions to vote for images");
            return;
        }
        String preCorrectionArtist = params.getArtist();
        ScrobbledArtist artist = CommandUtil.onlyCorrection(getService(), preCorrectionArtist, lastFM, false);
        List<VotingEntity> allArtistImages = getService().getAllArtistImages(artist.getArtistId());
        if (allArtistImages.isEmpty()) {
            sendMessageQueue(e, artist.getArtist() + " doesn't have any image");
            return;
        }
        String correctedArtist = CommandUtil.cleanMarkdownCharacter(allArtistImages.get(0).getArtist());
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(correctedArtist + " Images");

        AtomicInteger counter = new AtomicInteger(0);
        HashMap<String, BiFunction<VotingEntity, MessageReactionAddEvent, Boolean>> actionMap = new HashMap<>();
        List<Long> guildList = e.isFromGuild()
                ? getService().getAll(e.getGuild().getIdLong()).stream().filter(u -> !u.getRole().equals(Role.IMAGE_BLOCKED)).map(UsersWrapper::getDiscordID).collect(Collectors.toList())
                : List.of(e.getAuthor().getIdLong());

        actionMap.put(REPORT, (a, r) -> {
            if (guildList.contains(r.getUserIdLong())) {
                getService().report(a.getUrlId(), r.getUserIdLong());
            }
            return false;
        });
        actionMap.put(UP_VOTE, (a, r) -> {
            if (guildList.contains(r.getUserIdLong())) {
                VoteStatus voteStatus = getService().castVote(a.getUrlId(), r.getUserIdLong(), true);
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
                VoteStatus voteStatus = getService().castVote(a.getUrlId(), r.getUserIdLong(), false);
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
            actionMap.put(LEFT_ARROW, (aliasEntity, r) -> {
                int i = counter.decrementAndGet();
                if (i == 0) {
                    r.getReaction().clearReactions().queue();
                }
                if (i == allArtistImages.size() - 2) {
                    r.getChannel().addReactionById(r.getMessageIdLong(), RIGHT_ARROW).queue();
                }
                return false;
            });
            actionMap.put(RIGHT_ARROW, (a, r) -> {
                int i = counter.incrementAndGet();
                if (i == allArtistImages.size() - 1) {
                    r.getReaction().clearReactions().queue();
                }
                if (i == 1) {
                    r.getChannel().addReactionById(r.getMessageIdLong(), LEFT_ARROW).queue();
                }
                return false;
            });
        }

        new Validator<>(
                finalEmbed -> {
                    VotingEntity first = allArtistImages.stream().max(Comparator.comparingLong(VotingEntity::getVotes)).orElse(allArtistImages.get(0));
                    String description = "Submitted by: " + CommandUtil.getGlobalUsername(e.getJDA(), first.getOwner()) + "\n\n";
                    if (first != allArtistImages.get(0)) {
                        description += "The artist image for " + CommandUtil.cleanMarkdownCharacter(first.getArtist()) + " has changed to:";
                    } else {
                        description += "The top voted image for " + CommandUtil.cleanMarkdownCharacter(first.getArtist()) + " is:";
                    }
                    return finalEmbed.setTitle("Voting Timed Out")
                            .setImage(first.getUrl())
                            .clearFields()
                            .setDescription(description)
                            .setFooter(String.format("Has %d %s with %d%s", first.getVotes(), CommandUtil.singlePlural(first.getVotes(), "point", "points"), first.getTotalVotes(),
                                    CommandUtil.singlePlural(first.getTotalVotes(), " vote", " votes")))
                            .setColor(CommandUtil.randomColor());
                },
                () -> {
                    if (counter.get() >= allArtistImages.size() - 1) {
                        counter.set(allArtistImages.size() - 1);
                    }
                    if (counter.get() < 0) {
                        counter.set(0);
                    }
                    return allArtistImages.get(counter.get());
                },
                builder.apply(e.getJDA(), allArtistImages.size())
                , embedBuilder, e.getChannel(), e.getAuthor().getIdLong(), actionMap, true, true);

    }
}

