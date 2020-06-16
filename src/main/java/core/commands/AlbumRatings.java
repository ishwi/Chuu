package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.FullAlbumEntityExtended;
import dao.entities.Rating;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

public class AlbumRatings extends ConcurrentCommand<ArtistAlbumParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public AlbumRatings(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.RYM_BETA;
    }

    @Override
    public Parser<ArtistAlbumParameters> getParser() {

        return new ArtistAlbumParser(getService(), lastFM);
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        ArtistAlbumParameters parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(parse.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !parse.isNoredirect());
        String album = parse.getAlbum();
        String artist = parse.getArtist();

        dao.entities.AlbumRatings ratingss = getService().getRatingsByName(e.getGuild().getIdLong(), album, scrobbledArtist.getArtistId());

        NumberFormat formatter = new DecimalFormat("#0.#");
        NumberFormat average = new DecimalFormat("#0.##");

        FullAlbumEntityExtended chuu1 = lastFM.getAlbumSummary("chuu", scrobbledArtist.getArtist(), album);
        List<Rating> userRatings = ratingss.getUserRatings();
        String lastFmArtistAlbumUrl = CommandUtil.getLastFmArtistAlbumUrl(artist, album);
        List<String> stringList = userRatings.stream().filter(Rating::isSameGuild).map(x -> ". **[" +
                getUserString(e, x.getDiscordId()) +
                "](" + lastFmArtistAlbumUrl +
                ")** - " + formatter.format((float) x.getRating() / 2) +
                "\n").collect(Collectors.toList());
        if (stringList.isEmpty()) {
            sendMessageQueue(e, String.format("**%s** by **%s** was not rated by anyone.", album, artist));
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < stringList.size(); i++) {
            a.append(i + 1).append(stringList.get(i));
        }

        String chuu = chuu1.getAlbumUrl();
        List<Rating> servcerList = userRatings.stream().filter(Rating::isSameGuild).collect(Collectors.toList());
        String serverName = e.getGuild().getName();
        String botName = e.getJDA().getSelfUser().getName();
        String name = String.format("%s Average: **%s** | Ratings: **%d**", serverName, average.format(servcerList.stream().mapToDouble(rating -> rating.getRating() / 2f).average().orElse(0)), servcerList.size());
        String global = String.format("%s Average: **%s** | Ratings: **%d**", botName, average.format(userRatings.stream().mapToDouble(rating -> rating.getRating() / 2f).average().orElse(0)), userRatings.size());

        embedBuilder.setTitle(String.format("%s - %s Ratings in %s", chuu1.getArtist(), chuu1.getAlbum(), serverName), lastFmArtistAlbumUrl)

                .setFooter(String.format("%s%s has been rated by %d %s.", chuu1.getAlbum(),
                        ratingss.getReleaseYear() != null ? " (" + ratingss.getReleaseYear().toString() + ")" : ""
                        , userRatings.size(),
                        CommandUtil.singlePlural(userRatings.size(), "person", "people")))
                .setThumbnail(chuu)
                .setColor(CommandUtil.randomColor())
                .setDescription(a.toString() + "\n" + name + "\n" + global);

        e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(stringList, message1, embedBuilder));


    }
}
