package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.TrackDurationChart;
import core.apis.last.queues.TrackQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WastedTrackCommand extends ChartableCommand {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public WastedTrackCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
        this.parser.addOptional(new OptionalEntity("--notime", "dont display time spent"));
    }

    @Override
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new ChartGroupParameters(message, e);
    }


    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException {
        TrackQueue queue = new TrackQueue(getService(), discogsApi, spotifyApi, !params.isList());
        lastFM.getChart(params.getUsername(), params.getTimeFrameEnum().toApiFormat(), params.getX() * 2, params.getY() * 2,
                TopEntity.TRACK, TrackDurationChart.getTrackDurationParser((ChartGroupParameters) params), queue);
        int i = queue.setUp(params.getX() * params.getY());
        return new CountWrapper<>(i, queue);
    }

    @Override
    public String getDescription() {
        return "Tracks ordered by time spent";
    }

    @Override
    public List<String> getAliases() {
        return List.of("timetracks", "ttr", "ttra");
    }

    @Override
    public String getName() {
        return "Time on Tracks";
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s most listened tracks", embedBuilder,
                String.format(" has listened to songs for %s", String.format("%d:%02d hours", count / 3600, count / 60 % 60)));
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s most listened tracks" + time);
        return String.format("%s has listened to songs for %s%s (showing top %d songs)", initTitle,
                String.format("%d:%02d hours", count / 3600, count / 60 % 60), time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any track%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
