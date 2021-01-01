package core.commands.charts;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ArtistChart;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.TopParser;
import core.parsers.params.TopParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TopCommand extends ArtistAbleCommand<TopParameters> {
    public TopCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public ChartableParser<TopParameters> initParser() {
        return new TopParser(getService());
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(TopParameters params) throws LastFmException {
        BlockingQueue<UrlCapsule> queue;
        int count;
        if (params.isDoAlbum()) {
            queue = new ArrayBlockingQueue<>(params.getX() * params.getY());
            count = lastFM.getChart(params.getLastfmID(), new CustomTimeFrame(TimeFrameEnum.ALL), params.getX(), params.getY(), TopEntity.ALBUM, AlbumChart.getAlbumParser(params), queue);
        } else {
            queue = new ArtistQueue(getService(), discogsApi, spotifyApi, !params.isList());
            count = lastFM.getChart(params.getLastfmID(), new CustomTimeFrame(TimeFrameEnum.ALL), params.getX(), params.getY(), TopEntity.ARTIST, ArtistChart.getArtistParser(params), queue);
        }
        return new CountWrapper<>(count, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, TopParameters params, int count) {
        String s = params.isDoAlbum() ? "albums" : "artists";
        return params.initEmbed(String.format("'s top %s", s), embedBuilder, " has listened to " + count + " " + s, params.getLastfmID());
    }

    @Override
    public String configPieChart(PieChart pieChart, TopParameters params, int count, String initTitle) {
        String s = params.isDoAlbum() ? "albums" : "artists";
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%s's top %s%s", initTitle, s, time));
        return String.format("%s has listened to %d %s%s (showing top %d)", initTitle, count, time, s, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(TopParameters parameters) {
        String s = parameters.isDoAlbum() ? "albums" : "artists";
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any %s%s!", ingo.getUsername(), s, parameters.getTimeFrameEnum().getDisplayString()));
    }

    @Override
    public String getDescription() {
        return ("Your all time top artists/albums!");
    }

    @Override
    public String getName() {
        return "Top Artists/Albums Chart";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("top");
    }
}
