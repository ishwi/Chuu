package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.BlockingQueue;

public abstract class ArtistAbleCommand<T extends ChartParameters> extends ChartableCommand<T> {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;


    public ArtistAbleCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(T param) throws LastFmException {
        ArtistQueue queue = new ArtistQueue(getService(), discogsApi, spotifyApi, !param.isList());
        int i = param.makeCommand(lastFM, queue, TopEntity.ARTIST, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ARTIST, param, lastFM, param.getLastfmID()));
        return new CountWrapper<>(i, queue);
    }

    public void noElementsMessage(T parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any artist%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
