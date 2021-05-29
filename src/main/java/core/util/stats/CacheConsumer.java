package core.util.stats;

public interface CacheConsumer<T> {

    String consume(T item, StatsCtx data);
}
