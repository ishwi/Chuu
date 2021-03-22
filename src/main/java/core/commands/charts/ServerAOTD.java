package core.commands.charts;

import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.imagerenderer.util.pie.PieSetUp;
import core.parsers.ChartDecadeParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearRangeParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledAlbum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.awt.image.BufferedImage;
import java.time.Year;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ServerAOTD extends ChartableCommand<ChartYearRangeParameters> {


    public ServerAOTD(ChuuService dao) {
        super(dao);
        respondInPrivate = false;

    }

    @Override
    public ChartableParser<ChartYearRangeParameters> initParser() {
        return new ChartDecadeParser(db, 1);

    }

    @Override
    public String getDescription() {
        return "Like AOTD but for the whole server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("saotd", "serveraotd", "serveralbumofthedecade");
    }

    @Override
    public String getName() {
        return "Server AOTD";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartYearRangeParameters params) {
        if (!params.getTimeFrameEnum().isAllTime()) {
            sendMessageQueue(params.getE(), "Only alltime is supported for this command");
        }

        int x = params.getX();
        int y = params.getY();
        Year year = params.getBaseYear();
        int end = params.getNumberOfYears();

        int limit = params.isList() ? 200 : x * y;
        ResultWrapper<ScrobbledAlbum> albums = db.getCollectiveAOTD(params.getE().getGuild().getIdLong(), limit, params.isList() || params.isPieFormat(), year, end);
        List<ScrobbledAlbum> userAlbumByMbid = albums.getResultList();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        BlockingQueue<UrlCapsule> queue = userAlbumByMbid.stream()
                .map(z -> new AlbumChart(z.getUrl(), atomicInteger.getAndIncrement(), z.getAlbum(), z.getArtist(), z.getAlbumMbid(), z.getCount(), params.isWriteTitles(), params.isWritePlays(), params.isAside()))
                .limit(limit)
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        return new CountWrapper<>(albums.getRows(), queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartYearRangeParameters params, int count) {
        String s = params.getDisplayString();
        String titleInit = "'s top albums from " + s;
        String footerText = " has " + count + " albums from " + s;
        String name = params.getE().getGuild().getName();
        return embedBuilder.setAuthor(name + titleInit,
                null, params.getE().getGuild().getIconUrl())
                .setFooter(CommandUtil.markdownLessString(name) + footerText).setColor(CommandUtil.randomColor(params.getE()));
    }

    @Override
    public void doPie(PieChart pieChart, ChartYearRangeParameters gp, int count) {
        String subtitle = configPieChart(pieChart, gp, count, gp.getE().getGuild().getName());
        String urlImage = gp.getE().getGuild().getIconUrl();
        BufferedImage bufferedImage = new PieSetUp(subtitle, urlImage, pieChart).setUp();
        sendImage(bufferedImage, gp.getE());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartYearRangeParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%ss top albums from %s%s", initTitle, params.getDisplayString(), time));
        return String.format("%s has %d albums from %s (showing top %d)", initTitle, count, params.getDisplayString(), params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartYearRangeParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        sendMessageQueue(e, String.format("Couldn't find any %s album!", parameters.getDisplayString()));
    }

}
