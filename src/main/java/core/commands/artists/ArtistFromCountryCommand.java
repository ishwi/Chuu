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
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.CountryParser;
import core.parsers.Parser;
import core.parsers.params.ChartParameters;
import core.parsers.params.CountryParameters;
import core.parsers.utils.Optionals;
import core.services.MbidFetcher;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ArtistFromCountryCommand extends ConcurrentCommand<CountryParameters> {

    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    private final MusicBrainzService mb;

    public ArtistFromCountryCommand(ServiceView dao) {
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
        countryParser.addOptional(Optionals.IMAGE.opt);
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

    void doImage(List<ScrobbledArtist> list, CountryParameters countryParameters) {
        AtomicInteger ranker = new AtomicInteger(0);
        LastFMData data = countryParameters.getLastFMData();
        int size = data.getDefaultX() * data.getDefaultY();
        List<UrlCapsule> urlEntities = list.stream()
                .<UrlCapsule>map(w -> new ArtistChart(w.getUrl(), ranker.getAndIncrement(), w.getArtist(), null, w.getCount(), true, true, false))
                .takeWhile(x -> x.getPos() < size)
                .peek(x -> {
                    if (!StringUtils.isBlank(x.getUrl())) {
                        return;
                    }
                    try {
                        String artistImageUrl = new ArtistValidator(db, lastFM, countryParameters.getE()).validate(x.getArtistName()).getUrl();
                        x.setUrl(artistImageUrl);
                    } catch (LastFmException ignored) {
                    }
                })
                .toList();
        int rows = (int) Math.floor(Math.sqrt(urlEntities.size()));
        rows = Math.min(rows, data.getDefaultX());
        int cols = rows;

        BufferedImage image = CollageMaker.generateCollageThreaded(rows, cols, new ArrayBlockingQueue<>(urlEntities.size(), false, urlEntities), ChartQuality.PNG_BIG,
                false);
        sendImage(image, countryParameters.getE());

    }

    @Override
    public void onCommand(Context e, @Nonnull CountryParameters params) throws LastFmException {

        CountryCode country = params.getCode();
        LastFMData user = params.getLastFMData();
        String name = user.getName();
        long discordId = user.getDiscordId();
        List<ScrobbledArtist> userArtists;
        BlockingQueue<UrlCapsule> queue;
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
        List<ScrobbledArtist> list = new MbidFetcher(db, mb)
                .doFetch(
                        () -> userArtists,
                        (mbids) -> this.mb.getArtistFromCountry(country, mbids, null),
                        Comparator.comparingInt(ScrobbledArtist::getCount).reversed());
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, discordId);
        String userName = userInformation.username();
        String userUrl = userInformation.urlImage();
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

            doImage(list, params);
            return;
        }

        String title = userName + "'s top artists from " + countryRep + (":");
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setFooter(CommandUtil.unescapedUser(userName, discordId, e) + " has " + list.size() +
                        (list.size() == 1 ? " artist " : " artists ") + "from " + country.getName() + " " + usableTime, null)
                .setTitle(title, PrivacyUtils.getLastFmUser(params.getLastFMData().getName()));

        new PaginatorBuilder<>(e, embedBuilder, list).build().queue();


    }
}
