package core.commands.artists;


import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.BandRendered;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.pie.PieableListBand;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.utils.Optionals;
import core.services.CoverService;
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.AlbumUserPlays;
import dao.entities.ArtistAlbums;
import dao.entities.DiscordUserDisplay;
import dao.entities.ReturnNowPlaying;
import dao.entities.ScrobbledArtist;
import dao.entities.WrapperReturnNowPlaying;
import dao.exceptions.ChuuServiceException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.PieChart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class BandInfoCommand extends ConcurrentCommand<ArtistParameters> {
    private final PieableListBand pie;

    public BandInfoCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = true;
        pie = new PieableListBand(this.parser);
        order = 7;

    }

    protected <T extends ArtistParameters> void doDisplay(BandResult sr, T ap) {
        List<AlbumUserPlays> userTopArtistAlbums = sr.up().plays();
        long plays = sr.ap().artistPlays();
        WrapperReturnNowPlaying np = sr.wk().wr();


        boolean list = ap.hasOptional("list");
        boolean pie = ap.hasOptional("pie");
        ScrobbledArtist who = ap.getScrobbledArtist();
        Context e = ap.getE();
        long threshold = ap.getLastFMData().getArtistThreshold();

        ArtistAlbums ai = new ArtistAlbums(who.getArtist(), userTopArtistAlbums);
        CoverService cs = Chuu.getCoverService();
        for (AlbumUserPlays t : userTopArtistAlbums) {
            t.setAlbumUrl(cs.getCover(t.getArtist(), t.getAlbum(), t.getAlbumUrl(), e));
        }

        if (list) {
            doList(ap, ai);
            return;
        }

        for (ReturnNowPlaying element : np.getReturnNowPlayings()) {
            element.setDiscordName(CommandUtil.getUserInfoUnescaped(e, element.getDiscordId()).username());
        }
        BufferedImage logo = CommandUtil.getLogo(db, e);
        if (pie) {
            doPie(ap, np, ai, logo);
            return;
        }
        doImage(ap, np, ai, plays, threshold);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        ArtistParser ap = new ArtistParser(db, lastFM);
        ap.addOptional(Optionals.LIST.opt);
        ap.setExpensiveSearch(true);
        return ap;
    }

    @Override
    public String getDescription() {
        return "An image returning some information about an artist related to an user";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artist", "a");
    }

    protected void bandLogic(ArtistParameters ap) {

        final String lfmName = ap.getLastFMData().getName();
        Context e = ap.getE();
        ScrobbledArtist who = ap.getScrobbledArtist();

        int limit = ap.hasOptional("list") || ap.hasOptional("pie") ? Integer.MAX_VALUE : 9;


        try (var scope = new BandScope()) {
            scope.fork(() -> new Albums(db.getUserTopArtistAlbums(limit, who.getArtistId(), lfmName)));
            if (e.isFromGuild()) {
                scope.fork(() -> new WK(db.whoKnows(who.getArtistId(), e.getGuild().getIdLong(), 5)));
            } else {
                scope.fork(() -> new WK(db.globalWhoKnows(who.getArtistId(), 5, false, e.getAuthor().getIdLong(), false)));
            }
            scope.fork(() -> new AP(db.getArtistPlays(who.getArtistId(), lfmName)));
            scope.fork(() -> new AP(db.getArtistPlays(who.getArtistId(), lfmName)));
            scope.joinUntil(Instant.now().plus(15, ChronoUnit.SECONDS));

            BandResult sr = scope.result();
            doDisplay(sr, ap);

        } catch (StructuredNotHandledException | InterruptedException | TimeoutException ex) {
            throw new ChuuServiceException(ex);
        }


    }

    protected void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, long plays, long threshold) {
        np.setIndexes();
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, CommandUtil.getUserInfoUnescaped(ap.getE(), ap.getLastFMData().getDiscordId()).username(), threshold);
        sendImage(returnedImage, ap.getE());
    }

    protected void doList(ArtistParameters ap, ArtistAlbums ai) {
        Context e = ap.getE();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);

        List<String> lines = ai.getAlbumList().stream().map(x -> (String.format(". **[%s](%s)** - %d plays%n", x.getAlbum(), LinkUtils.getLastFmArtistAlbumUrl(ai.getArtist(), x.getAlbum()), x.getPlays()))).toList();

        configEmbedBuilder(embedBuilder, ap, ai);
        embedBuilder.
                setThumbnail(CommandUtil.noImageUrl(ap.getScrobbledArtist().getUrl()));

        new PaginatorBuilder<>(e, embedBuilder, lines).pageSize(10).build().queue();
    }

    protected void configEmbedBuilder(EmbedBuilder embedBuilder, ArtistParameters ap, ArtistAlbums ai) {
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoEscaped(ap.getE(), ap.getLastFMData().getDiscordId());
        embedBuilder.setTitle(uInfo.username() + "'s top " + CommandUtil.escapeMarkdown(ai.getArtist()) + " albums");

    }

    protected void doPie(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, BufferedImage logo) {
        PieChart pieChart = this.pie.doPie(ap, ai.getAlbumList());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(ap.getE(), ap.getLastFMData().getDiscordId());

        pieChart.setTitle(uInfo.username() + "'s top " + CommandUtil.escapeMarkdown(ap.getScrobbledArtist().getArtist()) + " albums");
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);

        int rgb = bufferedImage.getRGB(15, 15);
        Color color = new Color(rgb);
        int rows = Math.min(5, np.getRows());
        BufferedImage lastFmLogo = null;
        try (InputStream in = BandRendered.class.getResourceAsStream("/images/logo2.png")) {
            if (in != null) {
                lastFmLogo = ImageIO.read(in);
                lastFmLogo = Scalr.resize(lastFmLogo, 15);
            }
        } catch (IOException ignored) {
        }
        GraphicUtils.doChart(g, 10, 740 - rows * 25, 300, 25, rows, np, color, GraphicUtils.
                        setAlpha(Color.BLACK, 0.05f),
                lastFmLogo, true, new Font("Noto Sans CJK JP Light", Font.PLAIN, 18));
        g.drawImage(logo, 1000 - 85, 750 - 85, null);
        g.dispose();
        sendImage(bufferedImage, ap.getE());
    }

    @Override
    public String getName() {
        return "Artist";
    }

    @Override
    public void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);
        bandLogic(params);
    }

    public sealed interface BandStructure {

    }

    public record Albums(List<AlbumUserPlays> plays) implements BandStructure {
    }

    public record WK(WrapperReturnNowPlaying wr) implements BandStructure {
    }

    public record AP(long artistPlays) implements BandStructure {
    }

    public static class StructuredNotHandledException extends Exception {
        public StructuredNotHandledException(Throwable ex) {
            super(ex);
        }

        public StructuredNotHandledException() {

        }
    }

    public record BandResult(WK wk, Albums up, AP ap) {

    }

    public static class BandScope extends StructuredTaskScope<BandStructure> {
        private final AtomicBoolean finished = new AtomicBoolean(false);
        private volatile Albums p;
        private volatile WK wk;
        private volatile AP ap;
        private volatile Throwable ex;

        public BandScope() {
            super("bandscope", Thread.ofVirtual().name("bandscope-", 0).factory());
        }

        @Override
        protected void handleComplete(Subtask<? extends BandStructure> future) {
            switch (future.state()) {
                case SUCCESS -> {
                    switch (future.get()) {
                        case Albums u -> p = u;
                        case WK w -> this.wk = w;
                        case AP a -> this.ap = a;
                    }
                }
                case FAILED -> ex = future.exception();
                case UNAVAILABLE -> throw new ChuuServiceException(ex);
            }
        }

        @Override
        public StructuredTaskScope<BandStructure> join() throws InterruptedException {
            finished.set(true);
            return super.join();
        }

        @Override
        public StructuredTaskScope<BandStructure> joinUntil(Instant deadline) throws InterruptedException, TimeoutException {
            finished.set(true);
            return super.joinUntil(deadline);
        }

        public BandResult result() throws StructuredNotHandledException {
            if (!finished.get() || p == null || wk == null || ap == null || ex != null) {
                if (ex != null) {
                    throw new StructuredNotHandledException(ex);
                } else {
                    throw new StructuredNotHandledException();
                }

            }
            return new BandResult(wk, p, ap);
        }
    }


}
