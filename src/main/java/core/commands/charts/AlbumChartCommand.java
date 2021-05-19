package core.commands.charts;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AlbumChartCommand extends ChartableCommand<ChartParameters> {

    public AlbumChartCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    public ChartableParser<ChartParameters> initParser() {
        return new ChartParser(db);
    }

    @Override
    public String getDescription() {
        return "Returns a chart with album images";
    }

    @Override
    public List<String> getAliases() {
        return List.of("chart", "c");
    }

    @Override
    public String getName() {
        return "Chart";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        int i = param.makeCommand(lastFM, queue, TopEntity.ALBUM, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ALBUM, param, lastFM, param.getUser()));
        return new CountWrapper<>(i, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        String handleCount;
        if (!params.getTimeFrameEnum().isNormal()) {
            handleCount = "'s top " + count + " albums";
        } else {
            handleCount = " has listened to " + count + " albums";
        }
        return params.initEmbed("'s top albums", embedBuilder, handleCount, params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top albums" + time);
        if (!params.getTimeFrameEnum().isNormal()) {
            return String.format("%s top %d albums%s", initTitle, count, time);
        } else {
            return String.format("%s has listened to %d albums%s (showing top %d)", initTitle, count, time, params.getX() * params.getY());
        }
    }


    @Override
    public void noElementsMessage(ChartParameters parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any album%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }


}
