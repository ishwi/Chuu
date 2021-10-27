package core.commands.rym;


import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.AlbumRatings;
import dao.entities.Rating;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;

public class ArtistRatingsCommand extends ConcurrentCommand<ArtistParameters> {

    public ArtistRatingsCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "A list of albums rated of an artist in this server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistratings", "ratingsa", "ra", "ryma");
    }

    @Override
    public String getName() {
        return "Artist Ratings";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {


        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        String artist = sA.getArtist();
        List<AlbumRatings> rating = db.getArtistRatings(sA.getArtistId(), e.getGuild().getIdLong()).stream()
                .sorted(Comparator.comparingDouble((AlbumRatings y) -> y.userRatings().stream().filter(Rating::isSameGuild).mapToLong(Rating::getRating).average().orElse(0) * y.userRatings().size()).reversed()).toList();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);


        NumberFormat formatter = new DecimalFormat("#0.#");
        NumberFormat average = new DecimalFormat("#0.##");
        if (rating.isEmpty()) {
            sendMessageQueue(e, artist + " was not rated by anyone.");
            return;
        }


        long timesRated = rating.stream().mapToLong(z -> z.userRatings().stream().filter(Rating::isSameGuild).count()).sum();
        embedBuilder.setTitle(String.format("%s albums rated in %s", artist, e.getGuild().getName()), LinkUtils.getLastFmArtistUrl(artist))
                .setFooter(String.format("%s has been rated %d times in this server", artist, timesRated))
                .setThumbnail(sA.getUrl());


        Function<AlbumRatings, String> mapper = ratings -> {
            long userScore = ratings.userRatings().stream().filter(x -> x.getDiscordId() == params.getLastFMData().getDiscordId()).mapToLong(Rating::getRating).sum();
            List<Rating> serverList = ratings.userRatings().stream().filter(Rating::isSameGuild).toList();
            List<Rating> globalList = ratings.userRatings();
            OptionalDouble serverAverage = serverList.stream().mapToDouble(x -> x.getRating() / 2f).average();
            OptionalDouble globalAverage = globalList.stream().mapToDouble(x -> x.getRating() / 2f).average();
            String sAverage = serverAverage.isPresent() ? average.format(serverAverage.getAsDouble()) : "~~No Ratings~~";
            String gAverage = globalAverage.isPresent() ? average.format(globalAverage.getAsDouble()) : "~~No Ratings~~";
            String userString = userScore != 0 ? formatter.format((float) userScore / 2f) : "~~Unrated~~";
            String format = String.format("Your rating: **%s** \nServer Average: **%s** | # in server **%d**\n Global Average **%s** | # in total **%d**", userString, sAverage, serverList.size(), gAverage, globalList.size());
            String s = ratings.releaseYear() != null ? " \\(" + ratings.releaseYear() + "\\)" : "";
            return ". **[" + ratings.albumName() + s +
                    "](" + LinkUtils.getLastFmArtistAlbumUrl(artist, ratings.albumName()) +
                    ")** - " + format +
                    "\n\n";
        };

        new PaginatorBuilder<>(e, embedBuilder, rating).memoized(mapper).pageSize(5).build().queue();

    }
}
