package core.util.stats;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiFunction;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CacheHandler {
    private final Map<StatsGenerator<StatsCtx, ?>, Object> cache = new IdentityHashMap<>();

    private <T> T getT(StatsGenerator<StatsCtx, T> producer, StatsCtx ctx) {

        if (producer instanceof Combination c3) {
            Object walk = walk(c3, ctx);
            return (T) walk;
        } else {
            return getT1(producer, ctx);
        }
    }

    private <T, A, B> T walk(Combination<StatsCtx, A, StatsCtx, B, T> c, StatsCtx ctx) {
        StatsGenerator<StatsCtx, A> l = c.leftProcess();
        StatsGenerator<StatsCtx, B> b = c.getOther();
        BiFunction<A, B, T> otherMapping = c.getOtherMapping();
        if (l instanceof Combination b3) {
            A walk = (A) walk(b3, ctx);
            return c.getOtherMapping().apply(walk, getT1(c.getOther(), ctx));
        } else {
            StatsGenerator<StatsCtx, B> r = c.getOther();
            A t1 = getT1(l, ctx);
            B t11 = getT1(r, ctx);
            return c.getOtherMapping().apply(t1, t11);
        }
    }

    private <T> T getT1(StatsGenerator<StatsCtx, T> producer, StatsCtx ctx) {
        Object o = cache.get(producer);
        if (o != null) {
            return (T) o;
        } else {
            T process = producer.process(ctx);
            cache.put(producer, process);
            return process;
        }
    }

    public <T> String process(Cache<T> cache, StatsCtx ctx) {
        CacheConsumer<T> consumer = cache.consumer();
        return consumer.consume(getT(cache.producer(), ctx), ctx);
    }

    public <T> void initCache(StatsGenerator<StatsCtx, T> cacheConsumer, StatsCtx ctx) {
        getT(cacheConsumer, ctx);
    }

}
