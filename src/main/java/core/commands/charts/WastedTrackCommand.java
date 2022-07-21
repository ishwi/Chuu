package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.TrackQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartGroupParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartGroupParameters;
import core.util.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WastedTrackCommand extends ChartableCommand<ChartGroupParameters> {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public WastedTrackCommand(ServiceView dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartGroupParameters> initParser() {
        return new ChartGroupParser(db, TimeFrameEnum.WEEK);
    }

    @Override
    public String getSlashName() {
        return "songs";

    }


    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartGroupParameters params) throws LastFmException {
        TrackQueue queue = new TrackQueue(db, discogsApi, spotifyApi, !params.isList() && !params.isPie());
        lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), params.getX() * 2, params.getY() * 2,
                TopEntity.TRACK, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.TRACK, params, lastFM, params.getUser()), queue);
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
                String.format(" has listened to songs for %s", CommandUtil.secondFormatter(count)), params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartGroupParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s most listened tracks" + time);
        return String.format("%s has listened to songs for %s%s (showing top %d songs)", initTitle,
                CommandUtil.secondFormatter(count), time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartGroupParameters parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoEscaped(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any track%s!", ingo.username(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
