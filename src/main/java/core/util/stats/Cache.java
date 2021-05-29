package core.util.stats;

public record Cache<T>(StatsGenerator<StatsCtx, T> producer, CacheConsumer<T> consumer) {
}
