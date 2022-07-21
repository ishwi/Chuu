package core.commands.artists;

import com.neovisionaries.i18n.CountryCode;
import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.ArtistChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.imagerenderer.GraphicUtils;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyCountryParser;
import core.parsers.Parser;
import core.parsers.params.OnlyCountryParameters;
import core.parsers.utils.Optionals;
import core.services.MbidFetcher;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FromCountryServerCommand extends ConcurrentCommand<OnlyCountryParameters> {

    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    private final MusicBrainzService mb;

    public FromCountryServerCommand(ServiceView dao) {
        super(dao, true);
        mb = MusicBrainzServiceSingleton.getInstance();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<OnlyCountryParameters> initParser() {
        OnlyCountryParser countryParser = new OnlyCountryParser(db);
        countryParser.addOptional(Optionals.IMAGE.opt);
        return countryParser;
    }

    @Override
    public String getDescription() {
        return "Server top artist that are from a specific country";
    }

    @Override
    public String slashName() {
        return "from";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverfrom", "sfrom");
    }

    @Override
    public String getName() {
        return "Artist from a country in the server";
    }

    void doImage(List<ScrobbledArtist> list, OnlyCountryParameters countryParameters) {
        AtomicInteger ranker = new AtomicInteger(0);
        final int size;
        int size1;
        int x;
        int y;
        int x1;
        int y1;
        Context e = countryParameters.getE();
        try {
            LastFMData data = db.computeLastFmData(e.getAuthor().getIdLong(), e.getGuild().getIdLong());
            size1 = data.getDefaultX() * data.getDefaultY();
            x1 = data.getDefaultX();
            y1 = data.getDefaultY();
        } catch (InstanceNotFoundException ex) {
            size1 = 25;
            x1 = 5;
            y1 = 5;
        }
        x = x1;
        y = y1;
        size = size1;
        List<UrlCapsule> urlEntities = list.stream()
                .<UrlCapsule>map(w -> new ArtistChart(w.getUrl(), ranker.getAndIncrement(), w.getArtist(), null, w.getCount(), true, true, false))
                .takeWhile(t -> t.getPos() < size)
                .peek(t -> {
                    if (!StringUtils.isBlank(t.getUrl())) {
                        return;
                    }
                    try {
                        String artistImageUrl = new ArtistValidator(db, lastFM, e).validate(t.getArtistName()).getUrl();
                        t.setUrl(artistImageUrl);
                    } catch (LastFmException ignored) {
                        // Whatever
                    }
                })
                .toList();
        if (urlEntities.size() < x * y) {
            x = Math.max((int) Math.ceil(Math.sqrt(urlEntities.size())), 1);
            //noinspection SuspiciousNameCombination
            y = x;
        }

        ChartQuality quality = GraphicUtils.getQuality(urlEntities.size(), e);

        BufferedImage image = CollageMaker.generateCollageThreaded(x, y, new ArrayBlockingQueue<>(urlEntities.size(), false, urlEntities), ChartQuality.PNG_BIG,
                false);
        sendImage(image, e);

    }

    @Override
    public void onCommand(Context e, @Nonnull OnlyCountryParameters params) throws LastFmException {

        CountryCode country = params.getCode();
        MbidFetcher mbidFetcher = new MbidFetcher(db, mb);
        List<ScrobbledArtist> list = mbidFetcher
                .doFetch(
                        () -> this.db.getServerArtistsByMbid(e.getGuild().getIdLong()),
                        (mbids) -> this.mb.getArtistFromCountry(country, mbids, null),
                        Comparator.comparingInt(ScrobbledArtist::getCount).reversed());
        String guild = e.getGuild().getName();
        String userUrl = e.getGuild().getIconUrl();

        String countryRep;
        if (country.getAlpha2().equalsIgnoreCase("su")) {
            countryRep = "☭";
        } else {
            countryRep = ":flag_" + country.getAlpha2().toLowerCase();
        }
        if (list.isEmpty()) {
            sendMessageQueue(e, guild + " doesnt have any artist from " + countryRep + ": ");
            return;
        }
        if (params.hasOptional("image")) {
            AtomicInteger ranker = new AtomicInteger(0);
            doImage(list, params);
            return;
        }
        String title = guild + "'s top artists from " + countryRep + (":");
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setFooter(guild + " has " + list.size() +
                        (list.size() == 1 ? " artist " : " artists ") + "from " + country.getName(), null)
                .setTitle(title, PrivacyUtils.getLastFmUser(Chuu.DEFAULT_LASTFM_ID));

        new PaginatorBuilder<>(e, embedBuilder, list).build().queue();

    }
}
