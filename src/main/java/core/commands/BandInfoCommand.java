package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ChartUtil;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.BandRendered;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.PieableBand;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.params.ChartParameters;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class BandInfoCommand extends ConcurrentCommand<ArtistTimeFrameParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final PieableBand pie;

    public BandInfoCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
        this.respondInPrivate = true;
        pie = new PieableBand(this.parser);

    }

    @Override
    public Parser<ArtistTimeFrameParameters> getParser() {
        ArtistTimeFrameParser artistTimeFrameParser = new ArtistTimeFrameParser(getService(), lastFM);
        artistTimeFrameParser.addOptional(new OptionalEntity("--list", "display in list format"));
        artistTimeFrameParser.addOptional(new OptionalEntity("--noredirect", "not change the artist name for a correction automatically"));
        return artistTimeFrameParser;
    }

    @Override
    public String getDescription() {
        return "An image returning some information about an artist related to an user ";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artist", "a");
    }


    void bandLogic(ArtistTimeFrameParameters ap) throws InstanceNotFoundException, LastFmException {


        long idLong = ap.getUser().getIdLong();
        final String username = getService().findLastFMData(idLong).getName();


        ScrobbledArtist who = ap.getScrobbledArtist();
        int plays = getService().getArtistPlays(who.getArtistId(), username);
        MessageReceivedEvent e = ap.getE();
        if (plays == 0) {
            String username1 = CommandUtil.getUserInfoConsideringGuildOrNot(e, idLong).getUsername();
            parser.sendError(String.format("%s still hasn't listened to %s", username1, CommandUtil.cleanMarkdownCharacter(who.getArtist())), e);
            return;
        }
        BlockingQueue<UrlCapsule> urlCapsules = new LinkedBlockingDeque<>();
        lastFM.getChart(username, ap.getTimeFrame().toApiFormat(), 1500, 1, TopEntity.ALBUM,
                ChartUtil.getParser(ap.getTimeFrame(), TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, username), urlCapsules);
        List<UrlCapsule> albumsPlays = urlCapsules.stream().filter(x -> x.getArtistName().equalsIgnoreCase(who.getArtist())).collect(Collectors.toList());
        int sum = albumsPlays.stream().mapToInt(UrlCapsule::getPlays).sum();
        ArtistAlbums ai = new ArtistAlbums(who.getArtist(), albumsPlays.stream().map(x -> {

            AlbumUserPlays albumUserPlays = new AlbumUserPlays(x.getAlbumName(), x.getUrl());
            albumUserPlays.setPlays(x.getPlays());
            return albumUserPlays;
        }).collect(Collectors.toList()));


        if (sum <= 0.8 * plays && ap.getTimeFrame() == TimeFrameEnum.ALL) {
            ArtistAlbums ai2 = lastFM.getAlbumsFromArtist(who.getArtist(), 9);
            String artist = ai2.getArtist();
            List<AlbumUserPlays> list = ai2.getAlbumList();
            list =
                    list.stream()
                            .filter(x -> ai.getAlbumList().stream().noneMatch(y -> y.getAlbum().equalsIgnoreCase(x.getAlbum())))
                            .peek(albumInfo -> {
                                try {
                                    albumInfo.setPlays(lastFM.getPlaysAlbumArtist(username, artist, albumInfo.getAlbum())
                                            .getPlays());

                                } catch (LastFmException ex) {
                                    Chuu.getLogger().warn(ex.getMessage(), ex);
                                }
                            })
                            .filter(a -> a.getPlays() > 0)
                            .collect(Collectors.toList());
            list.sort(Comparator.comparing(AlbumUserPlays::getPlays).reversed());
            ai.setAlbumList(list);
            list.forEach(x -> x.setArtist(ai.getArtist()));
            ai.getAlbumList().addAll(list);
        }
        WrapperReturnNowPlaying np = getService().whoKnows(who.getArtistId(), e.getGuild().getIdLong(), 5);
        np.getReturnNowPlayings().forEach(element ->
                element.setDiscordName(CommandUtil.getUserInfoNotStripped(e, element.getDiscordId()).getUsername())
        );
        BufferedImage logo = CommandUtil.getLogo(getService(), e);


        if (ap.hasOptional("--list")) {
            doList(ap, ai);
            return;
        }
        if (ap.hasOptional("--pie")) {
            doPie(ap, np, ai, logo);
            return;
        }
        doImage(ap, np, ai, plays, logo);


    }

    private void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo) {
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, logo, CommandUtil.getUserInfoNotStripped(ap.getE(), ap.getUser().getIdLong()).getUsername());
        sendImage(returnedImage, ap.getE());
    }

    private void doList(ArtistTimeFrameParameters ap, ArtistAlbums ai) {
        MessageReceivedEvent e = ap.getE();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, ap.getUser().getIdLong());
        StringBuilder str = new StringBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        List<AlbumUserPlays> list = ai.getAlbumList();
        for (int i = 0; i < list.size(); i++) {
            AlbumUserPlays albumUserPlays = list.get(i);
            str.append(String.format("%d. [%s](%s) - %d plays%n", i + 1, albumUserPlays.getAlbum(), CommandUtil
                    .getLastFmArtistAlbumUrl(ai.getArtist(), albumUserPlays.getAlbum()), albumUserPlays.getPlays()));
        }
        embedBuilder.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.cleanMarkdownCharacter(ai.getArtist()) + " albums" + ap.getTimeFrame().getDisplayString()).
                setThumbnail(CommandUtil.noImageUrl(ap.getScrobbledArtist().getUrl())).setDescription(str)
                .setColor(CommandUtil.randomColor());
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel())
                .queue();
    }

    private void doPie(ArtistTimeFrameParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, BufferedImage logo) {
        PieChart pieChart = this.pie.doPie(ap, ai.getAlbumList());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(ap.getE(), ap.getUser().getIdLong());

        pieChart.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.cleanMarkdownCharacter(ap.getScrobbledArtist().getArtist()) + " albums" + ap.getTimeFrame().getDisplayString());
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

        ArtistTimeFrameParameters artistParameters = parser.parse(e);
        if (artistParameters == null)
            return;
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artistParameters.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify);
        artistParameters.setScrobbledArtist(scrobbledArtist);
        if (artistParameters.hasOptional("--noredirect")) {
            scrobbledArtist.setArtist(artistParameters.getArtist());
        }
        bandLogic(artistParameters);
    }


}
