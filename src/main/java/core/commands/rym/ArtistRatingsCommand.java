package core.commands.rym;


import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.AlbumRatings;
import dao.entities.Rating;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;

public class ArtistRatingsCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public ArtistRatingsCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
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
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {


        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        String artist = scrobbledArtist.getArtist();
        List<AlbumRatings> rating = db.getArtistRatings(scrobbledArtist.getArtistId(), e.getGuild().getIdLong()).stream()
                .sorted(Comparator.comparingDouble((AlbumRatings y) -> y.getUserRatings().stream().filter(Rating::isSameGuild).mapToLong(Rating::getRating).average().orElse(0) * y.getUserRatings().size()).reversed()).toList();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder();


        NumberFormat formatter = new DecimalFormat("#0.#");
        NumberFormat average = new DecimalFormat("#0.##");
        if (rating.isEmpty()) {
            sendMessageQueue(e, artist + " was not rated by anyone.");
            return;
        }
        AtomicInteger counter = new AtomicInteger(0);
        List<String> mappedString = rating.stream().map(ratings -> {
            long userScore = ratings.getUserRatings().stream().filter(x -> x.getDiscordId() == params.getLastFMData().getDiscordId()).mapToLong(Rating::getRating).sum();
            List<Rating> serverList = ratings.getUserRatings().stream().filter(Rating::isSameGuild).toList();
            List<Rating> globalList = ratings.getUserRatings();
            OptionalDouble serverAverage = serverList.stream().mapToDouble(x -> x.getRating() / 2f).average();
            OptionalDouble globalAverage = globalList.stream().mapToDouble(x -> x.getRating() / 2f).average();
            String sAverage = serverAverage.isPresent() ? average.format(serverAverage.getAsDouble()) : "~~No Ratings~~";
            String gAverage = globalAverage.isPresent() ? average.format(globalAverage.getAsDouble()) : "~~No Ratings~~";
            String userString = userScore != 0 ? formatter.format((float) userScore / 2f) : "~~Unrated~~";
            String format = String.format("Your rating: **%s** \nServer Average: **%s** | # in server **%d**\n Global Average **%s** | # in total **%d**", userString, sAverage, serverList.size(), gAverage, globalList.size());
            counter.addAndGet(serverList.size());
            String s = ratings.getReleaseYear() != null ? " \\(" + ratings.getReleaseYear().toString() + "\\)" : "";
            return ". **[" + ratings.getAlbumName() + s +
                   "](" + LinkUtils.getLastFmArtistAlbumUrl(artist, ratings.getAlbumName()) +
                   ")** - " + format +
                   "\n\n";
        }).toList();
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 5 && i < mappedString.size(); i++) {
            a.append(i + 1).append(mappedString.get(i));
        }


        embedBuilder.setTitle(String.format("%s albums rated in %s", artist, e.getGuild().getName()), LinkUtils.getLastFmArtistUrl(artist))
                .setFooter(String.format("%s has been rated %d times in this server", artist, counter.get()))
                .setDescription(a)
                .setColor(ColorService.computeColor(e))
                .setThumbnail(scrobbledArtist.getUrl());

        e.sendMessage(embedBuilder.build()).
                queue(message1 ->
                        new Reactionary<>(mappedString, message1, 5, embedBuilder));
    }
}
