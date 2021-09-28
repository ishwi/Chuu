package core.util.stats.generator;

import core.util.stats.Cache;
import core.util.stats.CacheConsumer;
import core.util.stats.StatsCtx;
import core.util.stats.StatsGenerator;
import dao.entities.CountWrapper;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GeneratorUtils {

    public static Cache<EMPTY> uInfo(Function<StatsCtx, String> consumer) {
        return new Cache<>(object -> new EMPTY(), (a, b) -> consumer.apply(b));
    }

    public static <T> Cache<T> gen(Function<StatsCtx, T> producer, BiFunction<T, StatsCtx, String> consumer) {
        return new Cache<>(producer::apply, consumer::apply);
    }

    public static Cache<CountWrapper<List<ScrobbledTrack>>> tL(BiFunction<CountWrapper<List<ScrobbledTrack>>, StatsCtx, String> consumer) {
        return new Cache<>(Generators.getGenerator(Generators.SONG_LIST), consumer::apply);
    }

    public static Cache<CountWrapper<List<ScrobbledAlbum>>> albL(BiFunction<CountWrapper<List<ScrobbledAlbum>>, StatsCtx, String> consumer) {
        return new Cache<>(Generators.getGenerator(Generators.ALBUM_LIST), consumer::apply);
    }

    public static Cache<CountWrapper<List<ScrobbledArtist>>> aL(BiFunction<CountWrapper<List<ScrobbledArtist>>, StatsCtx, String> consumer) {
        return new Cache<>(Generators.getGenerator(Generators.ARTIST_LIST), consumer::apply);
    }

    public static Cache<AllCached> all(CacheConsumer<AllCached> consumer) {
        StatsGenerator<StatsCtx, AllCached> combine =
                Generators.combine(
                        Generators.combine(
                                Generators.getGenerator(Generators.ARTIST_LIST),
                                Generators.getGenerator(Generators.ALBUM_LIST))
                        , Generators.getGenerator(Generators.SONG_LIST),
                        (Pair<CountWrapper<List<ScrobbledArtist>>, CountWrapper<List<ScrobbledAlbum>>> a, CountWrapper<List<ScrobbledTrack>> b)
                                -> new AllCached(a.getLeft(), a.getRight(), b));
        return new Cache<>(combine, consumer);
    }

    public static Cache<Np<ScrobbledArtist>> npArtist(CacheConsumer<Np<ScrobbledArtist>> consumer) {
        return np(Generators.getGenerator(Generators.ARTIST_LIST), consumer);
    }

    public static Cache<Np<ScrobbledArtist>> npAlbum(CacheConsumer<Np<ScrobbledArtist>> consumer) {
        return np(Generators.getGenerator(Generators.ALBUM_LIST), consumer);
    }

    public static Cache<Np<ScrobbledArtist>> npSong(CacheConsumer<Np<ScrobbledArtist>> consumer) {
        return np(Generators.getGenerator(Generators.SONG_LIST), consumer);
    }

    private static <T extends ScrobbledArtist, J extends CountWrapper<List<T>>> Cache<Np<T>> np(StatsGenerator<StatsCtx, J> item, CacheConsumer<Np<T>> consumer) {
        StatsGenerator<StatsCtx, Np<T>> combine =
                Generators.combine(
                        item,
                        Generators.getGenerator(Generators.NP),
                        (BiFunction<J, ScrobbledTrack, Np<T>>) Np::new);
        return new Cache<>(combine, consumer);
    }

    private static class EMPTY {
        private EMPTY() {
        }
    }

    public record AllCached(CountWrapper<List<ScrobbledArtist>> a, CountWrapper<List<ScrobbledAlbum>> al,
                            CountWrapper<List<ScrobbledTrack>> tr) {
    }

    public record Np<T extends ScrobbledArtist>(CountWrapper<List<T>> entities, ScrobbledTrack np) {
    }
}
