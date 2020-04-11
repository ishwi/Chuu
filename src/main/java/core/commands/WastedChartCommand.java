package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.TrackDurationArtistChart;
import core.apis.last.queues.GroupingQueue;
import core.apis.last.queues.TrackGroupArtistQueue;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.List;

public class WastedChartCommand extends GroupingChartCommand {


    public WastedChartCommand(ChuuService dao) {
        super(dao);
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
            lastFM.getChart(chartParameters.getLastfmID(), chartParameters.getTimeFrameEnum().toApiFormat(), 1499, 1, TopEntity.TRACK,
                    TrackDurationArtistChart.getTrackDurationArtistParser(ChartGroupParameters.toListParams()), queue);
        } else {
            queue = new TrackGroupArtistQueue(getService(), discogsApi, spotifyApi, chartParameters.getX() * chartParameters.getY());
            lastFM.getChart(chartParameters.getLastfmID(), chartParameters.getTimeFrameEnum().toApiFormat(), 1499, 1, TopEntity.TRACK,
                    TrackDurationArtistChart.getTrackDurationArtistParser(chartParameters), queue);
        }
        return new CountWrapper<>(-1, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartGroupParameters params, int count) {
        return params.initEmbed("'s most listened artists", embedBuilder,
                String.format(" has listened to artists for %s", String.format("%d:%02d hours", count / 3600, count / 60 % 60)));
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartGroupParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s most listened artists" + params.getTimeFrameEnum().getDisplayString());
        return String.format("%s has listened to %d artist (showing top %d)", initTitle, count, params.getX() * params.getY());

    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartGroupParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any artist%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}
