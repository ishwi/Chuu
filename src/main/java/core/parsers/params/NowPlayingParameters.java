package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TrackWithArtistId;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NowPlayingParameters extends ChuuDataParams {
    private final NowPlayingArtist nowPlayingArtist;
    private final CompletableFuture<List<TrackWithArtistId>> data;

    public NowPlayingParameters(MessageReceivedEvent e, LastFMData lastFMData, NowPlayingArtist nowPlayingArtist, CompletableFuture<List<TrackWithArtistId>> data) {
        super(e, lastFMData);
        this.nowPlayingArtist = nowPlayingArtist;
        this.data = data;
    }

    public NowPlayingArtist getNowPlayingArtist() {
        return nowPlayingArtist;
    }

    public CompletableFuture<List<TrackWithArtistId>> getData() {
        return data;
    }
}
