package core.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import core.Chuu;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HeavyCommandRateLimiter {

    private final static long MAX_SERVER = 2L;
    private final static long MAX_GLOBAL = 7L;
    private final static Map<Long, LocalDateTime> accesibleAgain = new HashMap<>();
    private static final LocalDateTime globalAccesibleAgain = LocalDateTime.now();
    private static final LoadingCache<Long, AtomicInteger> serverCache = CacheBuilder.newBuilder()
            .maximumSize(10000)

            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(notification -> {
                Object key = notification.getKey();
                if (key instanceof Long k)
                    accesibleAgain.remove(k);
            })
            .build(
                    new CacheLoader<>() {
                        public AtomicInteger load(@NotNull Long key) {
                            accesibleAgain.put(key, LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
                            return new AtomicInteger();
                        }
                    });
    private static final LoadingCache<Boolean, AtomicInteger> globalCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(100, TimeUnit.MINUTES)
            .removalListener(notification -> {
                Object key = notification.getKey();
                if (key instanceof Long k)
                    accesibleAgain.remove(k);
            })
            .build(
                    new CacheLoader<>() {
                        public AtomicInteger load(@NotNull Boolean key) {
                            return new AtomicInteger();
                        }
                    });

    public static RateLimited checkRateLimit(MessageReceivedEvent e) {
        try {
            AtomicInteger globalAdder = globalCache.get(true);
            if (globalAdder.incrementAndGet() >= MAX_GLOBAL) {
                return RateLimited.GLOBAL;
            } else {
                globalCache.refresh(true);
            }
            if (e.isFromGuild()) {
                AtomicInteger longAdder = serverCache.get(e.getGuild().getIdLong());
                if (longAdder.incrementAndGet() >= MAX_SERVER) {
                    return RateLimited.SERVER;
                } else {
                    serverCache.refresh(e.getGuild().getIdLong());
                }
            }
            return RateLimited.NONE;
        } catch (Exception exception) {
            // Impossible Exception
            Chuu.getLogger().warn(exception.getMessage(), exception);
            return RateLimited.NONE;
        }
    }

    public enum RateLimited {
        SERVER, GLOBAL, NONE
    }
}
