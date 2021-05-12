package core.commands.rym;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.FullAlbumEntityExtended;
import dao.entities.Rating;
import dao.entities.ScrobbledArtist;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.function.Function;

public class AlbumRatings extends ConcurrentCommand<ArtistAlbumParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public AlbumRatings(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
    }

    @NotNull
    public static Function<Byte, String> getStartsFromScore() {
        return (score) -> {
            float number = score / 2f;
            String starts = "★".repeat((int) number);
            if (number % 1 != 0)
                starts += "☆";
            return starts;
        };
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {

        return new ArtistAlbumParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Album Ratings of users that were uploaded to RYM";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rating", "rymal", "albumrating", "ral");
    }

    @Override
    public String getName() {
        return "Album Rating";
    }

    @Override
    protected void onCommand(Context e, @javax.validation.constraints.NotNull ArtistAlbumParameters params) throws LastFmException {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, false, !params.isNoredirect());
        String album = params.getAlbum();
        String artist = params.getArtist();

        dao.entities.AlbumRatings ratingss = db.getRatingsByName(e.getGuild().getIdLong(), album, scrobbledArtist.getArtistId());

        NumberFormat average = new DecimalFormat("#0.##");
        Function<Byte, String> starFormatter = getStartsFromScore();
        FullAlbumEntityExtended albumSummary = lastFM.getAlbumSummary(params.getLastFMData(), scrobbledArtist.getArtist(), album);
        List<Rating> userRatings = ratingss.getUserRatings();
        String lastFmArtistAlbumUrl = LinkUtils.getLastFmArtistAlbumUrl(artist, album);
        List<String> stringList = userRatings.stream().filter(Rating::isSameGuild).map(x -> ". **[" +
                getUserString(e, x.getDiscordId()) +
                "](" + lastFmArtistAlbumUrl +
                ")** - " + starFormatter.apply(x.getRating()) +
                "\n").toList();
        if (stringList.isEmpty()) {
            sendMessageQueue(e, String.format("**%s** by **%s** was not rated by anyone.", album, artist));
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < stringList.size(); i++) {
            a.append(i + 1).append(stringList.get(i));
        }

        String chuu = albumSummary.getAlbumUrl();
        List<Rating> servcerList = userRatings.stream().filter(Rating::isSameGuild).toList();
        String serverName = e.getGuild().getName();
        String botName = e.getJDA().getSelfUser().getName();
        String name = String.format("%s Average: **%s** | Ratings: **%d**", serverName, average.format(servcerList.stream().mapToDouble(rating -> rating.getRating() / 2f).average().orElse(0)), servcerList.size());
        String global = String.format("%s Average: **%s** | Ratings: **%d**", botName, average.format(userRatings.stream().mapToDouble(rating -> rating.getRating() / 2f).average().orElse(0)), userRatings.size());

        embedBuilder.setTitle(String.format("%s - %s Ratings in %s", albumSummary.getArtist(), albumSummary.getAlbum(), serverName), lastFmArtistAlbumUrl)

                .setFooter(String.format("%s%s has been rated by %d %s.", albumSummary.getAlbum(),
                        ratingss.getReleaseYear() != null ? " (" + ratingss.getReleaseYear().toString() + ")" : ""
                        , userRatings.size(),
                        CommandUtil.singlePlural(userRatings.size(), "person", "people")))
                .setThumbnail(chuu)
                .setColor(ColorService.computeColor(e))
                .setDescription(a + "\n" + name + "\n" + global);

        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(stringList, message1, embedBuilder));


    }
}
