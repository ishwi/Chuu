package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.entities.chartentities.TrackDurationArtistChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import dao.ChuuService;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TrackGroupArtistQueue extends GroupingQueue {
    public TrackGroupArtistQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, int requested) {
        super(dao, discogsApi, spotify, requested);
    }

    @Override
    public Function<UrlCapsule, String> mappingFunction() {
        return UrlCapsule::getArtistName;
    }

    @Override
    public BiFunction<UrlCapsule, UrlCapsule, UrlCapsule> reductorFunction() {
        return (urlCapsule, urlCapsule2) -> {
            urlCapsule.setPlays(urlCapsule.getPlays() + urlCapsule2.getPlays());
            if (urlCapsule instanceof TrackDurationArtistChart capsule) {
                if (urlCapsule2 instanceof TrackDurationArtistChart capsule2) {
                    capsule.setSeconds(capsule.getSeconds() + capsule2.getSeconds());
                    return capsule;
                }
            }
            return urlCapsule;
        };
    }

    @Override
    public Comparator<UrlCapsule> comparator() {
        return (x, y) -> {
            TrackDurationArtistChart x1 = (TrackDurationArtistChart) x;
            TrackDurationArtistChart y1 = (TrackDurationArtistChart) y;
            return Integer.compare(y1.getSeconds(), x1.getSeconds());
        };
    }
}
