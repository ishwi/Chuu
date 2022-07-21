package core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class ChuuVirtualPool {

    private static final Logger log = LoggerFactory.getLogger(ChuuVirtualPool.class);

    public static ScheduledExecutorService ofScheduled(int threads, String poolName) {
        AtomicInteger ranker = new AtomicInteger(0);
        return new ScheduledThreadPoolExecutor(threads,
                (t) -> new Thread(t, poolName + ranker.getAndIncrement()),
                new ChuuRejector(poolName));
    }


    public static ExecutorService of(String poolName) {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().uncaughtExceptionHandler((t, e) -> log.warn(e.getMessage(), e))
                .inheritInheritableThreadLocals(false)
                .allowSetThreadLocals(false)
                .name(poolName + "-Virtual-", 0).factory());
    }


}
