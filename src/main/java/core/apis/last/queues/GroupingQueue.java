package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GroupingQueue extends ArtistQueue {
    public final int requested;
    public Map<String, UrlCapsule> artistMap = new ConcurrentHashMap<>();
    public AtomicInteger counter = new AtomicInteger(0);

    public GroupingQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, int requested) {
        super(dao, discogsApi, spotify);
        this.requested = requested;
    }

    public abstract Function<UrlCapsule, String> mappingFunction();

    public abstract BiFunction<UrlCapsule, UrlCapsule, UrlCapsule> reductorFunction();

    public abstract Comparator<UrlCapsule> comparator();

    @Override
    public int size() {
        return wrapper.size();
    }

    @Override
    public boolean offer(@NotNull UrlCapsule item) {
        artistMap.merge(mappingFunction().apply(item), item, reductorFunction());
        return true;
    }

    public List<UrlCapsule> setUp() {
        List<UrlCapsule> collected = artistMap.values().stream().sorted(comparator())
                .takeWhile(urlCapsule -> {
                    int i = counter.getAndIncrement();
                    urlCapsule.setPos(i);
                    return i < requested;
                }).collect(Collectors.toList());
        collected.forEach(t -> wrapper.offer(CompletableFuture.supplyAsync(() ->
        {
            getUrl(t);
            return t;
        })));
        return collected;
    }

}
