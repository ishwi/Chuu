package core.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChuuFixedPool {

    public static ExecutorService of(int threads, String poolName) {
        return of(threads, poolName, 10);
    }

    public static ScheduledExecutorService ofScheduled(int threads, String poolName) {
        AtomicInteger ranker = new AtomicInteger(0);
        return new ScheduledThreadPoolExecutor(threads,
                (t) -> new Thread(t, poolName + ranker.getAndIncrement()),
                new ChuuRejector());
    }

    public static ExecutorService of(int threads, String poolName, int capacity) {
        AtomicInteger ranker = new AtomicInteger(0);
        return new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacity),
                (t) -> new Thread(t, poolName + ranker.getAndIncrement()),

                new ChuuRejector());
    }


}
