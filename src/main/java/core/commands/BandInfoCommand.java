package core.commands;


import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.BandRendered;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.PieableListBand;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BandInfoCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final PieableListBand pie;

    public BandInfoCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.respondInPrivate = true;
        pie = new PieableListBand(this.parser);

    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        ArtistParser artistTimeFrameParser = new ArtistParser(getService(), lastFM);
        artistTimeFrameParser.addOptional(new OptionalEntity("--list", "display in list format"));
        artistTimeFrameParser.setExpensiveSearch(true);
        return artistTimeFrameParser;
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

        boolean b = ap.hasOptional("--list");
        boolean b1 = ap.hasOptional("--pie");
        int limit = b || b1 ? Integer.MAX_VALUE : 4;
        ScrobbledArtist who = ap.getScrobbledArtist();
        List<AlbumUserPlays> userTopArtistAlbums = getService().getUserTopArtistAlbums(limit, who.getArtistId(), idLong);
        MessageReceivedEvent e = ap.getE();
        WrapperReturnNowPlaying np = getService().whoKnows(who.getArtistId(), e.getGuild().getIdLong(), 5);
        np.getReturnNowPlayings().forEach(element ->
                element.setDiscordName(CommandUtil.getUserInfoNotStripped(e, element.getDiscordId()).getUsername())
        );
        BufferedImage logo = CommandUtil.getLogo(getService(), e);
        ArtistAlbums ai = new ArtistAlbums(who.getArtist(), userTopArtistAlbums);

        if (b) {
            doList(ap, ai);
            return;
        }
        if (b1) {
            doPie(ap, np, ai, logo);
            return;
        }
        int plays = getService().getArtistPlays(who.getArtistId(), username);
        doImage(ap, np, ai, plays, logo);


    }

    void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo) {
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, logo, CommandUtil.getUserInfoNotStripped(ap.getE(), ap.getLastFMData().getDiscordId()).getUsername());
        sendImage(returnedImage, ap.getE());
    }

    void doList(ArtistParameters ap, ArtistAlbums ai) {
        MessageReceivedEvent e = ap.getE();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, ap.getLastFMData().getDiscordId());
        StringBuilder str = new StringBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        List<String> collect = ai.getAlbumList().stream().map(x -> (String.format(".[%s](%s) - %d plays%n", x.getAlbum(), CommandUtil
                .getLastFmArtistAlbumUrl(ai.getArtist(), x.getAlbum()), x.getPlays()))).collect(Collectors.toList());
        for (int i = 0; i < collect.size() && i < 10; i++) {
            String s = collect.get(i);
            str.append(i + 1).append(s);
        }
        embedBuilder.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.cleanMarkdownCharacter(ai.getArtist()) + " albums").
                setThumbnail(CommandUtil.noImageUrl(ap.getScrobbledArtist().getUrl())).setDescription(str)
                .setColor(CommandUtil.randomColor());
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build())
                .queue(message ->
                        new Reactionary<>(collect, message, 10, embedBuilder));
    }

    void doPie(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, BufferedImage logo) {
        PieChart pieChart = this.pie.doPie(ap, ai.getAlbumList());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(ap.getE(), ap.getLastFMData().getDiscordId());

        pieChart.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.cleanMarkdownCharacter(ap.getScrobbledArtist().getArtist()) + " albums");
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);

        int rgb = bufferedImage.getRGB(15, 15);
        Color color = new Color(rgb);
        int rows = Math.min(5, np.getRows());
        BufferedImage lastFmLogo;
        try (InputStream in = BandRendered.class.getResourceAsStream("/images/logo2.png")) {
            lastFmLogo = ImageIO.read(in);
            lastFmLogo = Scalr.resize(lastFmLogo, 15);
        } catch (IOException e) {
            lastFmLogo = null;
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        ArtistParameters artistParameters = parser.parse(e);
        if (artistParameters == null)
            return;
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artistParameters.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !artistParameters.isNoredirect());
        artistParameters.setScrobbledArtist(scrobbledArtist);
        bandLogic(artistParameters);
    }


}
