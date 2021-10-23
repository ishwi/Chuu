package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.RYMRatingParser;
import core.parsers.params.RYMRatingParams;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.RymStats;
import dao.entities.ScoredAlbumRatings;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class UserRatings extends ConcurrentCommand<RYMRatingParams> {
    public UserRatings(ServiceView dao) {
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
    protected void onCommand(Context e, @Nonnull RYMRatingParams params) {


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
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoEscaped(e, params.getLastFMData().getDiscordId());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).
                setTitle(userInfoConsideringGuildOrNot.username() + "'s albums rated with a **" + formatter.format(params.getRating() / 2f) + "**");
        List<String> stringList = new ArrayList<>();
        for (ScoredAlbumRatings x : myRatings) {
            String s = "# ***[" + CommandUtil.escapeMarkdown(x.getArtist()) + " - " + CommandUtil.escapeMarkdown(x.getName())
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
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoEscaped(e, params.getLastFMData().getDiscordId());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).
                setTitle(userInfoConsideringGuildOrNot.username() + "'s rating overview");
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
            s += indexer++ + ". ***[" + CommandUtil.escapeMarkdown(x.getArtist()) + " - " + CommandUtil.escapeMarkdown(x.getName())
                    +
                    "](" + LinkUtils.getLastFmArtistAlbumUrl(x.getArtist(), x.getName()) +
                    ")***\n\t" + String.format("Average: **%s** | # of Ratings: **%d**", formatter.format(x.getAverage() / 2f), x.getNumberOfRatings()) +
                    "\n";
            stringList.add(s);
        }

        build(params, e, formatter, embedBuilder, stringList, userInfoConsideringGuildOrNot);

    }

    private void build(RYMRatingParams params, Context e, NumberFormat formatter, EmbedBuilder embedBuilder, List<String> stringList, DiscordUserDisplay userInfoConsideringGuildOrNot) {
        RymStats stats = db.getUserRymStatms(params.getLastFMData().getDiscordId());
        embedBuilder
                .setThumbnail(userInfoConsideringGuildOrNot.urlImage())
                .setFooter(userInfoConsideringGuildOrNot.username() + " has rated " + stats.getNumberOfRatings() + " albums with an average of " + formatter.format(stats.getAverage() / 2f));

        new PaginatorBuilder<>(e, embedBuilder, stringList).pageSize(8).numberedEntries(false).build().queue();

    }
}
