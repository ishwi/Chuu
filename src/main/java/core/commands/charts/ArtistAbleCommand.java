package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import core.util.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;

import java.util.concurrent.BlockingQueue;

public abstract class ArtistAbleCommand<T extends ChartParameters> extends ChartableCommand<T> {
    final DiscogsApi discogsApi;
    final Spotify spotifyApi;


    public ArtistAbleCommand(ServiceView dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(T param) throws LastFmException {
        ArtistQueue queue = new ArtistQueue(db, discogsApi, spotifyApi, !param.isList() && !param.isPie());
        int i = param.makeCommand(lastFM, queue, TopEntity.ARTIST, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ARTIST, param, lastFM, param.getUser()));
        return new CountWrapper<>(i, queue);
    }

    public void noElementsMessage(T parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoEscaped(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any artist%s!", ingo.username(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
