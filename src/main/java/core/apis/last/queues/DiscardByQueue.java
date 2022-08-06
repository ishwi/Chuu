package core.apis.last.queues;

import core.apis.discogs.DiscogsApi;
import core.apis.last.entities.chartentities.PreComputedChartEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import core.commands.utils.CommandUtil;
import core.util.VirtualParallel;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TriFunction;
import dao.entities.UpdaterStatus;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscardByQueue extends DiscardableQueue<PreComputedChartEntity> {

    private static final TriFunction<ChuuService, DiscogsApi, Spotify, Predicate<PreComputedChartEntity>> innerFilter = (dao, discogsApi1, spotifyApi1) -> (PreComputedChartEntity item) -> {
        if (item.getUrl() == null) {
            getUrl(item, dao, discogsApi1, spotifyApi1);
        }
        return (item.getUrl() == null);
    };

    public DiscardByQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, Predicate<PreComputedChartEntity> discard, Function<UrlCapsule, PreComputedChartEntity> factoryFunction, int maxNumberOfElements) {
        this(dao, discogsApi, spotify, maxNumberOfElements, factoryFunction, innerFilter.apply(dao, discogsApi, spotify).or(discard), true);

    }


    public DiscardByQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, int maxNumberOfElements, Function<UrlCapsule, PreComputedChartEntity> factoryFunction, Predicate<PreComputedChartEntity> discard,
                          boolean needsImages) {
        super(maxNumberOfElements, factoryFunction, innerFilter.apply(dao, discogsApi, spotify).or(discard), needsImages);

    }

    private static void getUrl(@Nonnull UrlCapsule item, ChuuService dao, DiscogsApi discogsApi, Spotify spotifyApi) {
        fetchArtistURL(item, dao, discogsApi, spotifyApi);
    }

    static void fetchArtistURL(@Nonnull UrlCapsule item, ChuuService db, DiscogsApi discogsApi, Spotify spotifyApi) {
        try {
            UpdaterStatus updaterStatusByName = db.getUpdaterStatusByName(item.getArtistName());
            VirtualParallel.handleInterrupt();
            String url = updaterStatusByName.getArtistUrl();
            if (url == null) {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(item.getArtistName(), item.getPlays(), item.getUrl());
                scrobbledArtist.setArtistId(updaterStatusByName.getArtistId());
                url = CommandUtil.updateUrl(discogsApi, scrobbledArtist, db, spotifyApi);
            }
            item.setUrl(url);
        } catch (InstanceNotFoundException e) {
            //What can we do
        }
    }
}

