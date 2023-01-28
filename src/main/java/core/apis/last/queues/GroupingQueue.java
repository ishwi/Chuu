package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import core.util.VirtualParallel;
import dao.ChuuService;
import dao.exceptions.ChuuServiceException;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class GroupingQueue extends ArtistQueue {
    public final int requested;
    public final transient Map<String, UrlCapsule> artistMap = new HashMap<>();
    private final transient AtomicInteger counter = new AtomicInteger(0);
    boolean ready = false;
    int count = 0;

    public GroupingQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, int requested) {
        super(dao, discogsApi, spotify);
        this.requested = requested;
    }

    public abstract Function<UrlCapsule, String> mappingFunction();

    public abstract BiFunction<UrlCapsule, UrlCapsule, UrlCapsule> reductorFunction();

    public abstract Comparator<UrlCapsule> comparator();

    @Override
    public int size() {
        if (!ready)
            return artistMap.size();
        return count;
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
                }).toList();
        try (var scope = new VirtualParallel.ExecuteAllIgnoreErrors<UrlCapsule>()) {
            collected.forEach(t -> scope.fork(() -> {
                getUrl(t);
                return t;
            }));
            scope.joinUntil(Instant.now().plus(30, ChronoUnit.SECONDS));
        } catch (InterruptedException | TimeoutException e) {
            throw new ChuuServiceException(e);
        }

        this.ready = true;
        this.count = collected.size();
        return collected;
    }


}
