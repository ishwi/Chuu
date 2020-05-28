package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.TrackDurationChart;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TrackQueue extends ArtistQueue {

    public TrackQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, boolean doImages) {
        super(dao, discogsApi, spotify, doImages);
    }

    @Override
    public boolean offer(@NotNull UrlCapsule item) {
        if (item.getUrl() == null || item.getUrl().isBlank() || item.getUrl().equals(TrackGroupAlbumQueue.defaultTrackImage)) {
            return super.offer(item);
        }
        return wrapper.offer(CompletableFuture.supplyAsync(() -> item));
    }

    public int setUp(int limit) {
        ArrayList<CompletableFuture<UrlCapsule>> tempList = new ArrayList<>();
        wrapper.drainTo(tempList);
        List<UrlCapsule> collect = tempList.stream().map(x -> {
            try {
                return x.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        wrapper.clear();

        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger secondCounter = new AtomicInteger(0);

        wrapper.addAll(
                collect.stream().sorted((o1, o2) -> {
                    TrackDurationChart o11 = (TrackDurationChart) o1;
                    TrackDurationChart o12 = (TrackDurationChart) o2;
                    return Integer.compare(o12.getSeconds(), o11.getSeconds());
                }).peek(x -> {
                    TrackDurationChart z1 = (TrackDurationChart) x;
                    secondCounter.addAndGet(z1.getSeconds());
                    x.setPos(counter.getAndIncrement());
                }).takeWhile(y ->
                        y.getPos() < limit
                ).map(z -> CompletableFuture.supplyAsync(() -> z)
                ).collect(Collectors.toList()));
        return secondCounter.get();
    }
}
