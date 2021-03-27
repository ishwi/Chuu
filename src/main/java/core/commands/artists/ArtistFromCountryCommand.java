package core.commands.artists;

import com.neovisionaries.i18n.CountryCode;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.ArtistChart;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.otherlisteners.Reactionary;
import core.parsers.CountryParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChartParameters;
import core.parsers.params.CountryParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.ArtistUserPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ArtistFromCountryCommand extends ConcurrentCommand<CountryParameters> {

    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    private final MusicBrainzService mb;

    public ArtistFromCountryCommand(ChuuService dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<CountryParameters> initParser() {
        CountryParser countryParser = new CountryParser(db);
        countryParser.addOptional(new OptionalEntity("image", "show this with a chart instead of a list "));
        return countryParser;
    }

    @Override
    public String getDescription() {
        return "Your top artist that are from a specific country";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("from");
    }

    @Override
    public String getName() {
        return "Artist from a country";
    }

    void doImage(List<ArtistUserPlays> list, BlockingQueue<UrlCapsule> ogQuee, CountryParameters countryParameters) {
        AtomicInteger ranker = new AtomicInteger(0);
        List<UrlCapsule> urlEntities = ogQuee.stream()
                .filter(x -> list.stream().anyMatch(y -> y.getArtistName().equalsIgnoreCase(x.getArtistName())))
                .takeWhile(x -> {
                    x.setPos(ranker.getAndIncrement());
                    return x.getPos() < 25;
                })
                .peek(x -> {
                    try {
                        String artistImageUrl = CommandUtil.getArtistImageUrl(db, x.getArtistName(), lastFM, discogsApi, spotifyApi);
                        x.setUrl(artistImageUrl);
                    } catch (LastFmException ignored) {

                    }
                })
                .toList();
        int rows = (int) Math.floor(Math.sqrt(urlEntities.size()));
        rows = Math.min(rows, 5);
        int cols = rows;

        BufferedImage image = CollageMaker.generateCollageThreaded(rows, cols, new LinkedBlockingDeque<>(urlEntities), ChartQuality.PNG_BIG,
                false);
        sendImage(image, countryParameters.getE());

    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CountryParameters params) throws LastFmException {

        CountryCode country = params.getCode();
        LastFMData user = params.getLastFMData();
        String name = user.getName();
        Long discordId = user.getDiscordId();
        List<ScrobbledArtist> userArtists;
        BlockingQueue<UrlCapsule> queue = null;
        if (params.getTimeFrame().isAllTime()) {
            userArtists = this.db.getUserArtistByMbid(user.getName());
        } else {
            queue = new ArrayBlockingQueue<>(2000);
            lastFM.getChart(user, params.getTimeFrame(), 2000, 1, TopEntity.ARTIST, ChartUtil.getParser(params.getTimeFrame(), TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, user), queue);
            userArtists = queue.stream().map(x -> {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(x.getArtistName(), x.getPlays(), null);
                scrobbledArtist.setArtistMbid(x.getMbid());
                return scrobbledArtist;
            }).toList();
        }
        List<ArtistUserPlays> list = this.mb.getArtistFromCountry(country, userArtists, discordId);
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        String countryRep;
        if (country.getAlpha2().equalsIgnoreCase("su")) {
            countryRep = "☭";
        } else {
            countryRep = ":flag_" + country.getAlpha2().toLowerCase();
        }
        String usableTime = params.getTimeFrame().getDisplayString();
        if (list.isEmpty()) {
            sendMessageQueue(e, userName + " doesnt have any artist from " + countryRep + ": " + usableTime);
            return;
        }
        if (params.hasOptional("image")) {
            if (queue == null) {
                AtomicInteger ranker = new AtomicInteger(0);
                queue = userArtists.stream().map(t -> new ArtistChart(t.getUrl(), ranker.getAndIncrement(), t.getArtist(), t.getArtistMbid(), t.getCount(), true, true, false)).collect(Collectors.toCollection(LinkedBlockingDeque::new));
            }
            doImage(list, queue, params);
            return;
        }
        StringBuilder a = new StringBuilder();

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }

        String title = userName + "'s top artists from " + countryRep + (":");
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(userUrl)
                .setFooter(CommandUtil.markdownLessUserString(userName, discordId, e) + " has " + list.size() +
                        (list.size() == 1 ? " artist " : " artists ") + "from " + country.getName() + " " + usableTime, null)
                .setTitle(title)
                .setDescription(a);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(list, mes, embedBuilder));
    }
}
