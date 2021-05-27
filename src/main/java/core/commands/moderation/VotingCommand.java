package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.ButtonResult;
import core.otherlisteners.ButtonValidator;
import core.otherlisteners.Reaction;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.ColorService;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.Component;
import net.dv8tion.jda.api.interactions.button.Button;

import javax.validation.constraints.NotNull;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VotingCommand extends ConcurrentCommand<ArtistParameters> {
    private static final String RIGHT_ARROW = "➡";
    private static final String LEFT_ARROW = "⬅";
    private static final String UP_VOTE = "👍";
    private static final String DOWN_VOTE = "👎";
    private static final String REPORT = "🚫";
    private static final String CANCEL = "🏳️";


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
        String correctedArtist = CommandUtil.escapeMarkdown(allArtistImages.get(0).getArtist());

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle(correctedArtist + " Images");

        AtomicInteger counter = new AtomicInteger(0);
        Map<String, Reaction<VotingEntity, ButtonClickEvent, ButtonResult>> actionMap = new LinkedHashMap<>();
        List<Long> guildList = e.isFromGuild()
                               ? db.getAll(e.getGuild().getIdLong()).stream().filter(u -> !u.getRole().equals(Role.IMAGE_BLOCKED)).map(UsersWrapper::getDiscordID).toList()
                               : List.of(e.getAuthor().getIdLong());
        ActionRow of = ActionRow.of(Button.primary(UP_VOTE, Emoji.ofUnicode(UP_VOTE)),
                Button.primary(DOWN_VOTE, Emoji.ofUnicode(DOWN_VOTE)));
        List<ActionRow> rows = new ArrayList<>();
        rows.add(of);
        List<Component> components = of.getComponents();

        actionMap.put(UP_VOTE, (a, r) -> {
            if (guildList.contains(r.getUser().getIdLong())) {
                VoteStatus voteStatus = db.castVote(a.getUrlId(), r.getUser().getIdLong(), true);
                if (voteStatus.equals(VoteStatus.CHANGE_VALUE)) {
                    a.changeToAPositive();
                } else if (voteStatus.equals(VoteStatus.NEW_VOTE)) {
                    a.add();
                    a.incrementTotalVotes();
                }
            }
            return () -> new ButtonResult.Result(false, null);
        });
        actionMap.put(DOWN_VOTE, (a, r) -> {
            if (guildList.contains(r.getUser().getIdLong())) {
                VoteStatus voteStatus = db.castVote(a.getUrlId(), r.getUser().getIdLong(), false);
                if (voteStatus.equals(VoteStatus.CHANGE_VALUE)) {
                    a.changeToANegative();
                } else if (voteStatus.equals(VoteStatus.NEW_VOTE)) {
                    a.decrement();
                    a.incrementTotalVotes();
                }
            }
            return () -> new ButtonResult.Result(false, null);
        });

        if (allArtistImages.size() > 1) {

//            components.add(Button.primary(LEFT_ARROW, Emoji.ofUnicode(LEFT_ARROW)));
            components.add(Button.primary(RIGHT_ARROW, Emoji.ofUnicode(RIGHT_ARROW)));

            actionMap.put(LEFT_ARROW, (aliasEntity, r) -> leftMove(allArtistImages, counter, r, true));
            actionMap.put(RIGHT_ARROW, (a, r) -> rightMove(allArtistImages, counter, r, true));
        }

        actionMap.put(REPORT, (a, r) -> {
            if (guildList.contains(r.getUser().getIdLong())) {
                db.report(a.getUrlId(), r.getUser().getIdLong());
            }
            return () -> new ButtonResult.Result(false, null);
        });
        AtomicInteger size = new AtomicInteger(allArtistImages.size());

        if (lastFMData.getRole() == Role.ADMIN) {
            rows.add(ActionRow.of(Button.danger(CANCEL, Emoji.ofUnicode(CANCEL))));
            AtomicInteger deletedCounter = new AtomicInteger();
            actionMap.put(CANCEL, (a, r) -> {
                if (r.getUser().getIdLong() == e.getAuthor().getIdLong()) {
                    db.removeReportedImage(a.getUrlId(), a.getOwner(), r.getUser().getIdLong());
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
                return () -> new ButtonResult.Result(false, null);
            });
        }
        Button report = Button.danger(REPORT, Emoji.ofUnicode(REPORT));

        if (rows.size() == 2) {
            rows.get(1).getComponents().add(report);
        } else {
            rows.add(ActionRow.of(report));
        }

        new ButtonValidator<>(
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
                            description += "The artist image for " + CommandUtil.escapeMarkdown(first.getArtist()) + " has changed to:";
                        } else {
                            description += "The top voted image for " + CommandUtil.escapeMarkdown(first.getArtist()) + " is:";
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
                , embedBuilder, e, e.getAuthor().getIdLong(), actionMap, rows, true, true);

    }

    @org.jetbrains.annotations.NotNull
    private ButtonResult leftMove(List<VotingEntity> allArtistImages, AtomicInteger counter, ButtonClickEvent r, boolean isSame) {
        int i = counter.decrementAndGet();
        List<ActionRow> rows = r.getMessage().getActionRows();
        List<Component> arrowLess = rows.get(0).getComponents().stream().filter(z -> !(z.getId().equals(LEFT_ARROW) || z.getId().equals(RIGHT_ARROW))).collect(Collectors.toCollection(ArrayList::new));
        boolean changed = false;
        if (i == 0) {
            if (i != allArtistImages.size() - 1) {
                arrowLess.add(Button.primary(RIGHT_ARROW, Emoji.ofUnicode(RIGHT_ARROW)));
            }
            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
            changed = true;
        }
        if (i == allArtistImages.size() - 2) {
            if (i != 0) {
                arrowLess.add(Button.primary(LEFT_ARROW, Emoji.ofUnicode(LEFT_ARROW)));
            }
            arrowLess.add(Button.primary(RIGHT_ARROW, Emoji.ofUnicode(RIGHT_ARROW)));
            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
            changed = true;
        }
        if (!changed) {
            rows = null;
        }
        List<ActionRow> finalActionRows = rows;
        return () -> new ButtonResult.Result(false, finalActionRows);
    }

    @org.jetbrains.annotations.NotNull
    private ButtonResult rightMove(List<VotingEntity> allArtistImages, AtomicInteger counter, ButtonClickEvent r, boolean isSame) {

        int i = counter.incrementAndGet();

        List<ActionRow> rows = r.getMessage().getActionRows();
        List<Component> arrowLess = rows.get(0).getComponents().stream().filter(z -> !(z.getId().equals(LEFT_ARROW) || z.getId().equals(RIGHT_ARROW))).collect(Collectors.toCollection(ArrayList::new));
        boolean changed = false;

        if (i == 1) {
            arrowLess.add(Button.primary(LEFT_ARROW, Emoji.ofUnicode(LEFT_ARROW)));
            if (i != allArtistImages.size() - 1) {
                arrowLess.add(Button.primary(RIGHT_ARROW, Emoji.ofUnicode(RIGHT_ARROW)));

            }

            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
            changed = true;
        }

        if (i == allArtistImages.size() - 1) {
            arrowLess.add(Button.primary(LEFT_ARROW, Emoji.ofUnicode(LEFT_ARROW)));
            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
            changed = true;
        }


        if (!changed) {
            rows = null;
        }
        List<ActionRow> finalActionRows = rows;
        return () -> new ButtonResult.Result(false, finalActionRows);
    }
}

