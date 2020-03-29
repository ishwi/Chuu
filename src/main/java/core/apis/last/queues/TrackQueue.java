package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TrackQueue extends ArtistQueue {

    public TrackQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify) {
        super(dao, discogsApi, spotify);
    }

    @Override
    public boolean offer(@NotNull UrlCapsule item) {
        if (item.getUrl() == null) {
            return super.offer(item);
        }
        return wrapper.offer(CompletableFuture.supplyAsync(() -> item));
    }
}
