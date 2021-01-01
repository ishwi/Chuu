package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.last.queues.TrackQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartGroupParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartGroupParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WastedTrackCommand extends ChartableCommand<ChartGroupParameters> {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public WastedTrackCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartGroupParameters> initParser() {
        return new ChartGroupParser(getService(), TimeFrameEnum.WEEK);
    }


    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartGroupParameters params) throws LastFmException {
        TrackQueue queue = new TrackQueue(getService(), discogsApi, spotifyApi, !params.isList());
        lastFM.getChart(params.getLastfmID(), params.getTimeFrameEnum(), params.getX() * 2, params.getY() * 2,
                TopEntity.TRACK, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.TRACK, params, lastFM, params.getLastfmID()), queue);
        int i = queue.setUp(params.getX() * params.getY());
        return new CountWrapper<>(i, queue);
    }

    @Override
    public String getDescription() {
        return "Tracks ordered by time spent";
    }

    @Override
    public List<String> getAliases() {
        return List.of("timetracks", "ttr", "ttra", "timetrack");
    }

    @Override
    public String getName() {
        return "Time on Tracks";
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartGroupParameters params, int count) {
        return params.initEmbed("'s most listened tracks", embedBuilder,
                String.format(" has listened to songs for %s", String.format("%d:%02d hours", count / 3600, count / 60 % 60)), params.getLastfmID());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartGroupParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s most listened tracks" + time);
        return String.format("%s has listened to songs for %s%s (showing top %d songs)", initTitle,
                String.format("%d:%02d hours", count / 3600, count / 60 % 60), time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartGroupParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any track%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
