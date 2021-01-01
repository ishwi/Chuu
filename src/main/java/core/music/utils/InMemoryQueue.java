package core.music.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class InMemoryQueue {

    private final LoadingCache<Long, Queue<String>> controlAccess;

    public InMemoryQueue() {

        controlAccess = CacheBuilder.newBuilder().concurrencyLevel(2).expireAfterWrite(4, TimeUnit.HOURS).build(
                new CacheLoader<>() {
                    public Queue<String> load(@org.jetbrains.annotations.NotNull Long guild) {
                        return new ConcurrentLinkedQueue<>();
                    }
                });
    }


}
