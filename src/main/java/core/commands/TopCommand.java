package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ArtistChart;
import core.apis.last.queues.ArtistQueue;
import core.exceptions.LastFmException;
import core.parsers.OptionalEntity;
import core.parsers.TopParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TopParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TopCommand extends ArtistCommand {
    public TopCommand(ChuuService dao) {
        super(dao);
        this.parser = new TopParser(dao);
        parser.addOptional(new OptionalEntity("--artist", "use artists instead of albums"));
        parser.replaceOptional("--plays", new OptionalEntity("--noplays", "don't display plays"));
    }

    @Override
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new TopParameters(message, e);
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException {
        TopParameters top = (TopParameters) params;
        BlockingQueue<UrlCapsule> queue;
        int count;
        if (top.isDoArtist()) {
            queue = new ArrayBlockingQueue<>(top.getX() * top.getY());
            count = lastFM.getChart(top.getUsername(), "overall", top.getX(), top.getY(), TopEntity.ALBUM, AlbumChart.getAlbumParser(ChartParameters.toListParams()), queue);
        } else {
            queue = new ArtistQueue(getService(), discogsApi, spotifyApi, !top.isList());
            count = lastFM.getChart(params.getUsername(), "overall", top.getX(), top.getY(), TopEntity.ARTIST, ArtistChart.getArtistParser(ChartParameters.toListParams()), queue);
        }
        return new CountWrapper<>(count, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        String s = ((TopParameters) params).isDoArtist() ? "artists" : "albums";
        return params.initEmbed("'s top " + s, embedBuilder, " has listened to " + count + " " + s);
    }

    @Override
    public String getDescription() {
        return ("Your all time top albums!");
    }

    @Override
    public String getName() {
        return "Top Albums Chart";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("top");
    }
}
