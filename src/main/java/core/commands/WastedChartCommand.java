package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.TrackDurationArtistChart;
import core.apis.last.queues.GroupingQueue;
import core.apis.last.queues.TrackGroupArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class WastedChartCommand extends GroupingChartCommand {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;

    public WastedChartCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
    }


    @Override
    public String getDescription() {
        return "Chart with time spent on artists";
    }

    @Override
    public List<String> getAliases() {
        return List.of("timeartist", "tart", "tar", "ta");
    }


    @Override
    public String getName() {
        return "Artists ordered by listening time";
    }


    @Override
    public CountWrapper<GroupingQueue> processGroupedQueue(ChartGroupParameters chartParameters) throws LastFmException {
        GroupingQueue queue;

        if (chartParameters.isList()) {
            queue = new TrackGroupArtistQueue(getService(), discogsApi, spotifyApi, 200);
            lastFM.getChart(chartParameters.getUsername(), chartParameters.getTimeFrameEnum().toApiFormat(), 1499, 1, TopEntity.TRACK,
                    TrackDurationArtistChart.getTrackDurationArtistParser(ChartGroupParameters.toListParams()), queue);
        } else {
            queue = new TrackGroupArtistQueue(getService(), discogsApi, spotifyApi, chartParameters.getX() * chartParameters.getY());
            lastFM.getChart(chartParameters.getUsername(), chartParameters.getTimeFrameEnum().toApiFormat(), 1499, 1, TopEntity.TRACK,
                    TrackDurationArtistChart.getTrackDurationArtistParser(chartParameters), queue);
        }
        return new CountWrapper<>(-1, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s most listened artists", embedBuilder, " has listened to " + count + " artists");
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        sendMessageQueue(e, "Coundn't find any album");
    }
}
