package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TrackWithArtistId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NowPlayingParameters extends ChuuDataParams {
    private final NowPlayingArtist nowPlayingArtist;
    private final CompletableFuture<List<TrackWithArtistId>> data;

    public NowPlayingParameters(Context e, LastFMData lastFMData, NowPlayingArtist nowPlayingArtist, CompletableFuture<List<TrackWithArtistId>> data) {
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
