package core.commands.charts;

import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PieDoer;
import core.parsers.ChartDecadeParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearRangeParameters;
import core.util.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledAlbum;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.awt.image.BufferedImage;
import java.time.Year;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GlobalAOTD extends ChartableCommand<ChartYearRangeParameters> {

    public GlobalAOTD(ServiceView dao) {
        super(dao, true);
        respondInPrivate = true;

    }

    @Override
    public ChartableParser<ChartYearRangeParameters> initParser() {
        return new ChartDecadeParser(db);

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public String getSlashName() {
        return "aotd";
    }

    @Override
    public String getDescription() {
        return "Like AOTD but for the whole bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("gaotd", "globalaotd", "globalalbumofthedecade");
    }

    @Override
    public String getName() {
        return "Global AOTD";
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
        ResultWrapper<ScrobbledAlbum> albums = db.getCollectiveAOTD(null, limit, params.needCount(), year, end);
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
        String name = params.getE().getJDA().getSelfUser().getName();
        return embedBuilder.setAuthor(name + titleInit,
                        null, params.getE().getJDA().getSelfUser().getAvatarUrl())
                .setFooter(CommandUtil.stripEscapedMarkdown(name) + footerText);
    }

    @Override
    public void doPie(PieChart pieChart, ChartYearRangeParameters gp, int count) {
        String subtitle = configPieChart(pieChart, gp, count, gp.getE().getJDA().getSelfUser().getName());
        String urlImage = gp.getE().getJDA().getSelfUser().getAvatarUrl();
        BufferedImage bufferedImage = new PieDoer(subtitle, urlImage, pieChart).fill();
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
        Context e = parameters.getE();
        sendMessageQueue(e, String.format("Couldn't find any %s album!", parameters.getDisplayString()));
    }

}
