package core.commands.charts;

import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.ArtistChart;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.OnlyChartSizeParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartSizeParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TopCommand extends ArtistAbleCommand<ChartSizeParameters> {
    public TopCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    public ChartableParser<ChartSizeParameters> initParser() {
        OnlyChartSizeParser parser = new OnlyChartSizeParser(db, null, new OptionalEntity("album", "use albums"));
        parser.replaceOptional("plays", new OptionalEntity("noplays", "not show plays"));
        parser.addOptional(new OptionalEntity("plays", "shows this with plays", true, "noplays"));
        return parser;
    }

    @Override
    public String getSlashName() {
        return "top";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters params) throws LastFmException {
        BlockingQueue<UrlCapsule> queue;
        int count;
        if (params.hasOptional("album")) {
            queue = new ArrayBlockingQueue<>(params.getX() * params.getY());
            count = lastFM.getChart(params.getUser(), new CustomTimeFrame(TimeFrameEnum.ALL), params.getX(), params.getY(), TopEntity.ALBUM, AlbumChart.getAlbumParser(params), queue);
        } else {
            queue = new ArtistQueue(db, discogsApi, spotifyApi, !params.isList() && !params.isPie());
            count = lastFM.getChart(params.getUser(), new CustomTimeFrame(TimeFrameEnum.ALL), params.getX(), params.getY(), TopEntity.ARTIST, ArtistChart.getArtistParser(params), queue);
        }
        return new CountWrapper<>(count, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
        String s = params.hasOptional("album") ? "albums" : "artists";
        return params.initEmbed(String.format("'s top %s", s), embedBuilder, " has listened to " + count + " " + s, params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartSizeParameters params, int count, String initTitle) {
        String s = params.hasOptional("album") ? "albums" : "artists";
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%s's top %s%s", initTitle, s, time));
        return String.format("%s has listened to %d %s%s (showing top %d)", initTitle, count, time, s, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartSizeParameters params) {
        String s = params.hasOptional("album") ? "albums" : "artists";
        Context e = params.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any %s%s!", ingo.getUsername(), s, params.getTimeFrameEnum().getDisplayString()));
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
