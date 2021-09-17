package core.music.utils;

import core.apis.last.entities.Scrobble;
import core.music.sources.youtube.webscrobbler.processers.Processed;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public record TrackScrobble(Scrobble scrobble, List<Processed> processeds, String identifier, UUID uuid) {

    public TrackScrobble(InnerScrobble innerScroble, String identifier) {
        this(innerScroble.toScrobble(), innerScroble.processeds(), identifier, UUID.randomUUID());
    }

    public Times startEnd(long current, long total) {
        Map<Times, Processed> reduced = generateTimes(total);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        return reduced.entrySet().stream()
                .filter(m -> atomicInteger.incrementAndGet() > 0 && current > m.getKey().start && current < m.getKey().end)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(new Times(0, total));
    }

    @Nonnull
    private Map<Times, Processed> generateTimes(long total) {
        Map<Times, Processed> reduced = new TreeMap<>(Comparator.comparingLong(m -> m.start));
        processeds.stream().reduce((a, b) -> {
            reduced.put(new Times(a.msStart(), b.msStart()), a);
            return b;
        }).ifPresent(j -> reduced.put(new Times(j.msStart(), total), j));
        return reduced;
    }

    public DurationIndex mapDuration(long current, long total) {
        Map<Times, Processed> reduced = generateTimes(total);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        return reduced.entrySet().stream()
                .filter(m -> atomicInteger.incrementAndGet() > 0 && current > m.getKey().start && current < m.getKey().end)
                .findFirst()
                .map(g -> current - g.getKey().start)
                .map(z -> new DurationIndex(z, atomicInteger.get()))
                .orElse(new DurationIndex(current, -1));
    }

    public Scrobble scrobble(long currentMS, long totalMS) {
        if (processeds.size() == 1) {
            return scrobble;
        }
        for (int i = 0, processedsSize = processeds.size(); i < processedsSize; i++) {
            Processed current = processeds.get(i);
            long duration;
            if (i == processedsSize - 1) {
                duration = totalMS - current.msStart();
            } else {
                Processed next = processeds.get(i + 1);
                if (next.msStart() < currentMS) {
                    continue;
                }
                duration = next.msStart() - current.msStart();
            }
            return new Scrobble(current.artist(), current.album(), current.song(), scrobble.image(), duration);
        }
        return scrobble;
    }

    public TrackScrobble withProcess(List<Processed> processeds) {
        return new TrackScrobble(this.scrobble, processeds, this.identifier, this.uuid);
    }

    public record DurationIndex(long duration, int index) {
    }

    public record Times(long start, long end) {
    }
}
