package core.commands.charts;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AlbumChartCommand extends ChartableCommand<ChartParameters> {

    public AlbumChartCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public ChartableParser<ChartParameters> initParser() {
        return new ChartParser(getService());
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
        int i = param.makeCommand(lastFM, queue, TopEntity.ALBUM, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ALBUM, param, lastFM, param.getLastfmID()));
        return new CountWrapper<>(i, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        String handleCount;
        if (!params.getTimeFrameEnum().isNormally()) {
            handleCount = "'s top " + count + " albums";
        } else {
            handleCount = " has listened to " + count + " albums";
        }
        return params.initEmbed("'s top albums", embedBuilder, handleCount, params.getLastfmID());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top albums" + time);
        if (!params.getTimeFrameEnum().isNormally()) {
            return String.format("%s top %d albums%s", initTitle, count, time);
        } else {
            return String.format("%s has listened to %d albums%s (showing top %d)", initTitle, count, time, params.getX() * params.getY());
        }
    }


    @Override
    public void noElementsMessage(ChartParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any album%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }


}
