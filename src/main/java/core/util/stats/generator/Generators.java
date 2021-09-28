package core.util.stats.generator;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.NPService;
import core.services.validators.TrackValidator;
import core.util.stats.StatsCtx;
import core.util.stats.StatsGenerator;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

enum Generators {
    // Consumers exploited as singletons for caching
    ARTIST_LIST(ctx -> {
        if (ctx.timeFrameEnum().isAllTime()) {
            var artists = ctx.chuuService().getAllUserArtist(ctx.lastFMData().getDiscordId());
            int size = artists.size();
            return new CountWrapper<>(size, artists);
        } else {
            return getList(TopEntity.ARTIST, ctx, ScrobbledArtist.class);
        }
    }),
    ALBUM_LIST(ctx -> {
        if (ctx.timeFrameEnum().isAllTime()) {
            var artists = ctx.chuuService().getUserAlbums(ctx.lastFMData().getName());
            return new CountWrapper<>(artists.size(), artists);
        } else {
            return getList(TopEntity.ALBUM, ctx, ScrobbledAlbum.class);
        }
    }),
    SONG_LIST(ctx -> {
        if (ctx.timeFrameEnum().isAllTime()) {
            var songs = ctx.chuuService().getTopTracks(ctx.lastFMData().getName());
            return new CountWrapper<>(songs.size(), songs);
        } else {
            return getList(TopEntity.TRACK, ctx, ScrobbledTrack.class);
        }
    }),
    USER_INFO(StatsCtx::userInfo),
    NP(data -> {
        if (data.np() == null) {
            try {
                NowPlayingArtist nowPlaying = new NPService(data.lastFM(), data.lastFMData()).getNowPlaying();
                return new TrackValidator(data.chuuService(), data.lastFM()).fromNP(nowPlaying);
            } catch (InstanceNotFoundException | LastFmException e) {
                throw new StatsCalculationException();
            }
        } else {
            return data.np();
        }
    });

    private final StatsGenerator<StatsCtx, ?> generator;

    Generators(StatsGenerator<StatsCtx, ?> generator) {
        this.generator = generator;
    }

    @SuppressWarnings("unchecked")
    public static <T> StatsGenerator<StatsCtx, T> getGenerator(Generators generators) {
        return (StatsGenerator<StatsCtx, T>) generators.generator;

    }

    static <Y, Z, W> StatsGenerator<StatsCtx, W> combine(
            StatsGenerator<StatsCtx, Y> first,
            StatsGenerator<StatsCtx, Z> second,
            BiFunction<Y, Z, W> mapper) {
        return first.or(second, mapper);
    }

    static <T, Y, Z> StatsGenerator<StatsCtx, Pair<Y, Z>> combine(
            StatsGenerator<StatsCtx, Y> first,
            StatsGenerator<StatsCtx, Z> second) {
        return combine(first, second, Pair::of);
    }

    private static <T extends ScrobbledArtist> CountWrapper<List<T>> getList(TopEntity topEntity, StatsCtx ctx, Class<T> clazz) {
        CustomTimeFrame ctfe = ctx.timeFrameEnum();
        try {
            BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(2000);
            int chart = ctx.lastFM().getChart(ctx.lastFMData(), ctfe, 2000, 1, topEntity, ChartUtil.getParser(ctfe, topEntity, ChartParameters.toListParams(), ctx.lastFM(), ctx.lastFMData()), queue);
            return new CountWrapper<>(chart, queue.stream().map(t -> map(t, topEntity, clazz)).toList());
        } catch (LastFmException e) {
            throw new StatsCalculationException();
        }
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends ScrobbledArtist> T map(UrlCapsule capsule, TopEntity topEntity, Class<T> clazz) {
        return (T) switch (topEntity) {
            case ALBUM -> new ScrobbledAlbum(capsule.getArtistName(), capsule.getPlays(), null, -1, capsule.getAlbumName(), null);
            case TRACK -> new ScrobbledTrack(capsule.getArtistName(), capsule.getAlbumName(), capsule.getPlays(), false, -1, null, null, null);
            case ARTIST -> new ScrobbledArtist(capsule.getArtistName(), capsule.getPlays(), null);
        };
    }


}
