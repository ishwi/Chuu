package core.apis.last.queues;

import com.wrapper.spotify.SpotifyApi;
import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.PreComputedChartEntity;
import core.apis.spotify.Spotify;
import core.commands.CommandUtil;
import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TriFunction;
import dao.entities.UpdaterStatus;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
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

    private static void getUrl(@NotNull UrlCapsule item, ChuuService dao, DiscogsApi discogsApi, Spotify spotifyApi) {
        try {
            UpdaterStatus updaterStatusByName = dao.getUpdaterStatusByName(item.getArtistName());
            String url = updaterStatusByName.getArtistUrl();
            if (url == null) {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(item.getArtistName(), item.getPlays(), item.getUrl());
                scrobbledArtist.setArtistId(updaterStatusByName.getArtistId());
                url = CommandUtil.updateUrl(discogsApi, scrobbledArtist, dao, spotifyApi);
            }
            item.setUrl(url);
        } catch (InstanceNotFoundException e) {
            //What can we do
        }
    }
}

