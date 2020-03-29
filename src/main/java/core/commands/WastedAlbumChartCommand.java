package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.TrackDurationAlbumArtistChart;
import core.apis.last.queues.GroupingQueue;
import core.apis.last.queues.TrackGroupAlbumQueue;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
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
        int albumsQueried = lastFM.getChart(params.getUsername(), params.getTimeFrameEnum().toApiFormat(), 1499, 1, TopEntity.ALBUM, AlbumChart.getAlbumParser(ChartParameters.toListParams()), albumQueu);
        List<UrlCapsule> albumList = new ArrayList<>(albumQueu.size());
        albumQueu.drainTo(albumList);
        GroupingQueue queue;
        if (params.isList()) {
            queue = new TrackGroupAlbumQueue(getService(), discogsApi, spotifyApi, 200, albumList);
        } else {
            queue = new TrackGroupAlbumQueue(getService(), discogsApi, spotifyApi, params.getX() * params.getY(), albumList);
        }
        lastFM.getChart(params.getUsername(), params.getTimeFrameEnum().toApiFormat(), 1499, 1, TopEntity.TRACK, TrackDurationAlbumArtistChart.getParser(params), queue);
        return new CountWrapper<>(albumsQueried, queue);
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s most listened albums", embedBuilder, " has listened to " + count + " albums");
    }

    @Override
    public void configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s most listened albums" + params.getTimeFrameEnum().getDisplayString());
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
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
        return List.of("timealbums", "talb", "tal");

    }


}
