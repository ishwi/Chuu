package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ArtistChart;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ArtistCommand extends ChartableCommand {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;


    public ArtistCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters param) throws LastFmException {
        ArtistQueue queue = new ArtistQueue(getService(), discogsApi, spotifyApi, !param.isList());
        int i = param.makeCommand(lastFM, queue, TopEntity.ARTIST, ArtistChart.getArtistParser(param));
        return new CountWrapper<>(i, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top artists", embedBuilder, " has listened to " + count + " artists");
    }

    @Override
    public String getDescription() {
        return "Returns a chart with artist images";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artchart", "charta", "artistchart", "ca");
    }


    @Override
    public String getName() {
        return "Artist Chart";
    }


    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any artist%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}
