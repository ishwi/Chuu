package core.commands.artists;


import core.Chuu;
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
import core.imagerenderer.BandRendered;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.pie.PieableListBand;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ServiceView;
import dao.entities.*;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class BandInfoCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final PieableListBand pie;

    public BandInfoCommand(ServiceView dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.respondInPrivate = true;
        pie = new PieableListBand(this.parser);
        order = 7;

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        ArtistParser ap = new ArtistParser(db, lastFM);
        ap.addOptional(new OptionalEntity("list", "show as a list"));
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


    void bandLogic(ArtistParameters ap) {

        long idLong = ap.getLastFMData().getDiscordId();
        final String username = ap.getLastFMData().getName();
        long threshold = ap.getLastFMData().getArtistThreshold();

        boolean b = ap.hasOptional("list");
        boolean b1 = ap.hasOptional("pie");
        int limit = b || b1 ? Integer.MAX_VALUE : 9;
        ScrobbledArtist who = ap.getScrobbledArtist();
        List<AlbumUserPlays> userTopArtistAlbums = db.getUserTopArtistAlbums(limit, who.getArtistId(), idLong);
        Context e = ap.getE();

        ArtistAlbums ai = new ArtistAlbums(who.getArtist(), userTopArtistAlbums);
        userTopArtistAlbums.forEach(t -> t.setAlbumUrl(Chuu.getCoverService().

                getCover(t.getArtist(), t.

                        getAlbum(), t.

                        getAlbumUrl(), e)));

        if (b || !e.isFromGuild()) {
            doList(ap, ai);
            return;
        }

        WrapperReturnNowPlaying np = db.whoKnows(who.getArtistId(), e.getGuild().getIdLong(), 5);
        np.getReturnNowPlayings().

                forEach(element ->
                        element.setDiscordName(CommandUtil.getUserInfoUnescaped(e, element.getDiscordId()).

                                getUsername())
                );
        BufferedImage logo = CommandUtil.getLogo(db, e);
        if (b1) {
            doPie(ap, np, ai, logo);
            return;
        }

        int plays = db.getArtistPlays(who.getArtistId(), username);

        doImage(ap, np, ai, plays, logo, threshold);


    }


    void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo, long threshold) {
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, logo, CommandUtil.getUserInfoUnescaped(ap.getE(), ap.getLastFMData().getDiscordId()).getUsername(), threshold);
        sendImage(returnedImage, ap.getE());
    }

    void doList(ArtistParameters ap, ArtistAlbums ai) {
        Context e = ap.getE();
        StringBuilder str = new StringBuilder();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        List<String> lines = ai.getAlbumList().stream().map(x -> (String.format(". **[%s](%s)** - %d plays%n", x.getAlbum(), LinkUtils.getLastFmArtistAlbumUrl(ai.getArtist(), x.getAlbum()), x.getPlays()))).toList();
        for (int i = 0; i < lines.size() && i < 10; i++) {
            String s = lines.get(i);
            str.append(i + 1).append(s);
        }
        configEmbedBuilder(embedBuilder, ap, ai);
        embedBuilder.
                setThumbnail(CommandUtil.noImageUrl(ap.getScrobbledArtist().getUrl())).setDescription(str);
        e.sendMessage(embedBuilder.build())
                .queue(message ->
                        new Reactionary<>(lines, message, 10, embedBuilder));
    }

    void configEmbedBuilder(EmbedBuilder embedBuilder, ArtistParameters ap, ArtistAlbums ai) {
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(ap.getE(), ap.getLastFMData().getDiscordId());
        embedBuilder.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.escapeMarkdown(ai.getArtist()) + " albums");

    }

    void doPie(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, BufferedImage logo) {
        PieChart pieChart = this.pie.doPie(ap, ai.getAlbumList());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(ap.getE(), ap.getLastFMData().getDiscordId());

        pieChart.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.escapeMarkdown(ap.getScrobbledArtist().getArtist()) + " albums");
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
                        makeMoreTransparent(Color.BLACK, 0.05f),
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
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        params.setScrobbledArtist(scrobbledArtist);
        bandLogic(params);
    }


}
