package core.commands.charts;

import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.imagerenderer.GraphicUtils;
import core.parsers.ChartYearParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.*;
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

public class ServerAOTY extends ChartableCommand<ChartYearParameters> {

    private final MusicBrainzService mb;

    public ServerAOTY(ChuuService dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();

    }

    @Override
    public ChartableParser<ChartYearParameters> initParser() {
        return new ChartYearParser(getService(), TimeFrameEnum.ALL);

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
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        if (!params.getTimeFrameEnum().isAllTime()) {
            sendMessageQueue(params.getE(), "Only alltime is supported for this command");
        }
        List<AlbumInfo> nonEmptyMbid;

        ResultWrapper<ScrobbledAlbum> rapper = getService().getGuildAlbumTop(params.getE().getGuild().getIdLong(), 4_000, false);
        List<ScrobbledAlbum> userAlbumByMbid = rapper.getResultList();
        AtomicInteger atomicInteger = new AtomicInteger(0);

        nonEmptyMbid = userAlbumByMbid.stream()
                .peek(x -> queue.add(new AlbumChart(x.getUrl(), atomicInteger.getAndIncrement(), x.getAlbum(), x.getArtist(), x.getAlbumMbid(), x.getCount(), params.isWriteTitles(), params.isWritePlays(), params.isAside())))
                .map(x -> new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist()))
                .filter(albumInfo -> !(albumInfo.getMbid() == null || albumInfo.getMbid().isEmpty()))
                .collect(Collectors.toList());


        Year year = params.getYear();
        List<AlbumInfo> albumsMbizMatchingYear;


        albumsMbizMatchingYear = mb.listOfYearReleases(nonEmptyMbid, year);

        AtomicInteger counter2 = new AtomicInteger(0);
        boolean b = queue.removeIf(urlCapsule ->

        {
            for (AlbumInfo albumInfo : albumsMbizMatchingYear) {
                if ((!albumInfo.getMbid().isEmpty() && albumInfo.getMbid().equals(urlCapsule.getMbid())) || urlCapsule
                        .getAlbumName().equalsIgnoreCase(albumInfo.getName()) && urlCapsule.getArtistName()
                        .equalsIgnoreCase(albumInfo.getArtist())) {
                    urlCapsule.setPos(counter2.getAndAdd(1));
                    return false;
                }
            }
            return true;
        });
        int min = Math.min(5, (int) Math.floor(Math.sqrt(queue.size())));
        params.setX(min);
        params.setY(min);

        queue.removeIf(capsule -> capsule.getPos() >= params.getX() * params.getY());
        return new CountWrapper<>(albumsMbizMatchingYear.size(), queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartYearParameters params, int count) {
        String s = params.getYear().toString();
        String titleInit = "'s top albums from " + s;
        String footerText = " has " + count + " albums from " + s;
        String name = params.getE().getGuild().getName();
        return embedBuilder.setAuthor(name + titleInit,
                null, params.getE().getGuild().getIconUrl())
                .setFooter(CommandUtil.markdownLessString(name) + footerText).setColor(CommandUtil.randomColor());
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
