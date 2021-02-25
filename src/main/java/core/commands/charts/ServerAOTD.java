package core.commands.charts;

import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.imagerenderer.GraphicUtils;
import core.parsers.ChartYearParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledAlbum;
import dao.entities.TimeFrameEnum;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.Year;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ServerAOTD extends ChartableCommand<ChartYearParameters> {

    private final MusicBrainzService mb;

    public ServerAOTD(ChuuService dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();

    }

    @Override
    public ChartableParser<ChartYearParameters> initParser() {
        return new ChartYearParser(db, TimeFrameEnum.ALL);

    }

    @Override
    public String getDescription() {
        return "Like Aoty but for the whole server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("saoty", "serveraoty", "serveralbumoftheyear");
    }

    @Override
    public String getName() {
        return "Server AOTY";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartYearParameters params) {
        if (!params.getTimeFrameEnum().isAllTime()) {
            sendMessageQueue(params.getE(), "Only alltime is supported for this command");
        }

        int x = params.getX();
        int y = params.getY();
        Year year = params.getYear();

        int limit = params.isList() ? 200 : x * y;
        ResultWrapper<ScrobbledAlbum> albums = db.getCollectiveAOTY(params.getE().getGuild().getIdLong(), limit, params.isList() || params.isPieFormat(), year);
        List<ScrobbledAlbum> userAlbumByMbid = albums.getResultList();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        BlockingQueue<UrlCapsule> queue = userAlbumByMbid.stream()
                .map(z -> new AlbumChart(z.getUrl(), atomicInteger.getAndIncrement(), z.getAlbum(), z.getArtist(), z.getAlbumMbid(), z.getCount(), params.isWriteTitles(), params.isWritePlays(), params.isAside()))
                .limit(limit)
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        return new CountWrapper<>(albums.getRows(), queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartYearParameters params, int count) {
        String s = params.getYear().toString();
        String titleInit = "'s top albums from " + s;
        String footerText = " has " + count + " albums from " + s;
        String name = params.getE().getGuild().getName();
        return embedBuilder.setAuthor(name + titleInit,
                null, params.getE().getGuild().getIconUrl())
                .setFooter(CommandUtil.markdownLessString(name) + footerText).setColor(CommandUtil.randomColor(params.getE()));
    }

    @Override
    public void doPie(PieChart pieChart, ChartYearParameters gp, int count) {
        String urlImage;
        String subtitle;
        subtitle = configPieChart(pieChart, gp, count, gp.getE().getGuild().getName());
        urlImage = gp.getE().getGuild().getIconUrl();
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        Font annotationsFont = pieChart.getStyler().getAnnotationsFont();
        pieChart.paint(g, 1000, 750);
        g.setFont(annotationsFont.deriveFont(11.0f));
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(subtitle, g);
        g.drawString(subtitle, 1000 - 10 - (int) stringBounds.getWidth(), 740 - 2);
        GraphicUtils.inserArtistImage(urlImage, g);
        sendImage(bufferedImage, gp.getE());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartYearParameters params, int count, String initTitle) {
        Year year = params.getYear();
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%ss top albums from %s%s", initTitle, year.toString(), time));
        return String.format("%s has %d albums from %s (showing top %d)", initTitle, count, year.toString(), params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartYearParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        sendMessageQueue(e, String.format("Couldn't find any %s album!", parameters.getYear().toString()));
    }

}
