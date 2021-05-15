package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.RYMRatingParser;
import core.parsers.params.RYMRatingParams;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.RymStats;
import dao.entities.ScoredAlbumRatings;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class UserRatings extends ConcurrentCommand<RYMRatingParams> {
    public UserRatings(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<RYMRatingParams> initParser() {
        return new RYMRatingParser(db);
    }

    @Override
    public String getDescription() {
        return "Your RYM Ratings";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ratings", "rymprofile", "rymp");
    }

    @Override
    public String getName() {
        return "RYM Ratings";
    }

    @Override
    protected void onCommand(Context e, @NotNull RYMRatingParams params) {


        Short rating = params.getRating();
        List<ScoredAlbumRatings> myRatings = db.getSelfRatingsScore(params.getLastFMData().getDiscordId(), rating);

        if (rating == null) {
            listWithRating(myRatings, params);
        } else {
            listOnlyOneRating(myRatings, params);
        }

    }

    private void listOnlyOneRating(List<ScoredAlbumRatings> myRatings, RYMRatingParams params) {
        Context e = params.getE();
        NumberFormat formatter = new DecimalFormat("#0.##");
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder().
                setTitle(userInfoConsideringGuildOrNot.getUsername() + "'s albums rated with a **" + formatter.format(params.getRating() / 2f) + "**");
        List<String> stringList = new ArrayList<>();
        for (ScoredAlbumRatings x : myRatings) {
            String s = "# ***[" + CommandUtil.cleanMarkdownCharacter(x.getArtist()) + " - " + CommandUtil.cleanMarkdownCharacter(x.getName())
                       +
                       "](" + LinkUtils.getLastFmArtistAlbumUrl(x.getArtist(), x.getName()) +
                       ")***\n\t" + String.format("Average: **%s** | # of Ratings: **%d**", formatter.format(x.getAverage() / 2f), x.getNumberOfRatings()) +
                       "\n";
            stringList.add(s);
        }

        build(params, e, formatter, embedBuilder, stringList, userInfoConsideringGuildOrNot);
    }

    private void listWithRating(List<ScoredAlbumRatings> myRatings, RYMRatingParams params) {
        Context e = params.getE();
        NumberFormat formatter = new DecimalFormat("#0.##");
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder().
                setTitle(userInfoConsideringGuildOrNot.getUsername() + "'s rating overview");
        List<String> stringList = new ArrayList<>();
        double prevRating = -1d;
        int indexer = 0;
        for (ScoredAlbumRatings x : myRatings) {
            String s = "";
            if (x.getScore() != prevRating) {
                s += "\n**Albums rated with a " + formatter.format(x.getScore() / 2f) + "**:\n";
                prevRating = x.getScore();
                indexer = 1;
            }
            s += indexer++ + ". ***[" + CommandUtil.cleanMarkdownCharacter(x.getArtist()) + " - " + CommandUtil.cleanMarkdownCharacter(x.getName())
                 +
                 "](" + LinkUtils.getLastFmArtistAlbumUrl(x.getArtist(), x.getName()) +
                 ")***\n\t" + String.format("Average: **%s** | # of Ratings: **%d**", formatter.format(x.getAverage() / 2f), x.getNumberOfRatings()) +
                 "\n";
            stringList.add(s);
        }

        build(params, e, formatter, embedBuilder, stringList, userInfoConsideringGuildOrNot);

    }

    private void build(RYMRatingParams params, Context e, NumberFormat formatter, EmbedBuilder embedBuilder, List<String> stringList, DiscordUserDisplay userInfoConsideringGuildOrNot) {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 8 && i < stringList.size(); i++) {
            a.append(stringList.get(i));
        }
        RymStats stats = db.getUserRymStatms(params.getLastFMData().getDiscordId());
        embedBuilder
                .setColor(ColorService.computeColor(e))
                .setThumbnail(userInfoConsideringGuildOrNot.getUrlImage())
                .setFooter(userInfoConsideringGuildOrNot.getUsername() + " has rated " + stats.getNumberOfRatings() + " albums with an average of " + formatter.format(stats.getAverage() / 2f))
                .setDescription(a);
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(stringList, message1, 8, embedBuilder, false));
    }
}
