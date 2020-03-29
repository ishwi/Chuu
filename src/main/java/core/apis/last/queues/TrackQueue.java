package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.TrackDurationChart;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        if (item.getUrl() == null) {
            return super.offer(item);
        }
        return wrapper.offer(CompletableFuture.supplyAsync(() -> item));
    }

    public void setUp(int limit) {
        ArrayList<CompletableFuture<UrlCapsule>> tempList = new ArrayList<>();
        wrapper.drainTo(tempList);
        wrapper.clear();
        AtomicInteger counter = new AtomicInteger(0);
        wrapper.addAll(tempList.stream().sorted((o1, o2) -> {
            try {
                TrackDurationChart o11 = (TrackDurationChart) o1.get();
                TrackDurationChart o12 = (TrackDurationChart) o2.get();
                return Integer.compare(o12.getSeconds(), o11.getSeconds());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return -1;

        }).peek(x -> {
            try {
                x.get().setPos(counter.getAndIncrement());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).takeWhile(urlCapsuleCompletableFuture -> {
            try {
                return urlCapsuleCompletableFuture.get().getPos() < limit;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toList()));

    }
}
