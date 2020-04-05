package core.commands;

import core.Chuu;
import core.commands.util.PieableBand;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.BandRendered;
import core.imagerenderer.GraphicUtils;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BandInfoCommand extends WhoKnowsCommand {

    private final PieableBand pie;

    public BandInfoCommand(ChuuService dao) {
        super(dao);
        pie = new PieableBand(this.parser);
    }

    @Override
    public String getDescription() {
        return "An image returning some information about an artist related to an user ";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artist", "a");
    }


    @Override
    void whoKnowsLogic(ArtistParameters ap) throws InstanceNotFoundException, LastFmException {
        ArtistAlbums ai;

        final String username = getService().findLastFMData(ap.getDiscordId()).getName();


        ScrobbledArtist who = ap.getScrobbledArtist();
        int plays = getService().getArtistPlays(who.getArtistId(), username);
        MessageReceivedEvent e = ap.getE();
        if (plays == 0) {
            String username1 = CommandUtil.getUserInfoConsideringGuildOrNot(e, ap.getDiscordId()).getUsername();
            parser.sendError(String.format("%s still hasn't listened to %s", username1, CommandUtil.cleanMarkdownCharacter(who.getArtist())), e);
            return;
        }

        ai = lastFM.getAlbumsFromArtist(who.getArtist(), 14);
        String artist = ai.getArtist();
        List<AlbumUserPlays> list = ai.getAlbumList();
        list =
                list.stream().peek(albumInfo -> {
                    try {
                        albumInfo.setPlays(lastFM.getPlaysAlbum_Artist(username, artist, albumInfo.getAlbum())
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
            doPie(ap, np, ai, plays, logo);
            return;
        }
        doImage(ap, np, ai, plays, logo);


    }

    private void doImage(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo) {
        BufferedImage returnedImage = BandRendered
                .makeBandImage(np, ai, plays, logo, CommandUtil.getUserInfoNotStripped(ap.getE(), ap.getDiscordId()).getUsername());
        sendImage(returnedImage, ap.getE());
    }

    private void doList(ArtistParameters ap, ArtistAlbums ai) {
        MessageReceivedEvent e = ap.getE();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, ap.getDiscordId());
        StringBuilder str = new StringBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        List<AlbumUserPlays> list = ai.getAlbumList();
        for (int i = 0; i < list.size(); i++) {
            AlbumUserPlays albumUserPlays = list.get(i);
            str.append(String.format("%d. [%s](%s) - %d plays\n", i + 1, albumUserPlays.getAlbum(), CommandUtil
                    .getLastFmArtistAlbumUrl(ai.getArtist(), albumUserPlays.getAlbum()), albumUserPlays.getPlays()));
        }
        embedBuilder.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.cleanMarkdownCharacter(ai.getArtist()) + " albums").
                setThumbnail(CommandUtil.noImageUrl(ap.getScrobbledArtist().getUrl())).setDescription(str)
                .setColor(CommandUtil.randomColor());
        //.setFooter("Command invoked by " + event.getMember().getLastFmId().getDiscriminator() + "" + LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE).toApiFormat(), );
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel())
                .queue();
    }

    private void doPie(ArtistParameters ap, WrapperReturnNowPlaying np, ArtistAlbums ai, int plays, BufferedImage logo) {
        PieChart pieChart = this.pie.doPie(ap, ai.getAlbumList());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(ap.getE(), ap.getDiscordId());

        pieChart.setTitle(uInfo.getUsername() + "'s top " + CommandUtil.cleanMarkdownCharacter(ap.getScrobbledArtist().getArtist()) + " album ");
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


}
