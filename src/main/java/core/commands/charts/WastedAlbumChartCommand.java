package core.commands.charts;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.GroupingQueue;
import core.apis.last.queues.TrackGroupAlbumQueue;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class WastedAlbumChartCommand extends GroupingChartCommand {
    public WastedAlbumChartCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    public CountWrapper<GroupingQueue> processGroupedQueue(ChartGroupParameters params) throws LastFmException {
        BlockingQueue<UrlCapsule> albumQueu = new LinkedBlockingDeque<>();
        int albumsQueried = lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), 1499, 1, TopEntity.ALBUM, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, params.getUser()), albumQueu);
        List<UrlCapsule> albumList = new ArrayList<>(albumQueu.size());
        albumQueu.drainTo(albumList);
        GroupingQueue queue;
        if (params.isList()) {
            queue = new TrackGroupAlbumQueue(getService(), discogsApi, spotifyApi, 200, albumList);
        } else {
            queue = new TrackGroupAlbumQueue(getService(), discogsApi, spotifyApi, params.getX() * params.getY(), albumList);
        }
        lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), 1499, 1, TopEntity.TRACK, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ALBUM, params, lastFM, params.getUser()), queue);
        return new CountWrapper<>(albumsQueried, queue);
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartGroupParameters params, int count) {
        return params.initEmbed("'s most listened albums", embedBuilder,
                String.format(" has listened albums for %s", String.format("%d:%02d hours", count / 3600, count / 60 % 60)), params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartGroupParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s most listened albums" + time);

        return String.format("%s has listened to albums for %s%s (showing top %d albums)", initTitle,
                String.format("%d:%02d hours", count / 3600, count / 60 % 60), time, params.getX() * params.getY());

    }

    @Override
    public void noElementsMessage(ChartGroupParameters parameters) {
        MessageReceivedEvent e = parameters.getE();

        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any album%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }

    @Override
    public String getName() {
        return "Time on albums";
    }

    @Override
    public String getDescription() {
        return "Albums ordered by listening time";
    }

    @Override
    public List<String> getAliases() {
        return List.of("timealbums", "talb", "tal", "timealbum");

    }


}
