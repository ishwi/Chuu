package core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChuuVirtualPool {

    private static final Logger log = LoggerFactory.getLogger(ChuuVirtualPool.class);
    private static final ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 64, 2, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), Thread.ofPlatform()
            .name("global-workaround-pool")
            .factory());

    public static ScheduledExecutorService ofScheduled(int threads, String poolName) {
        AtomicInteger ranker = new AtomicInteger(0);
        return new ScheduledThreadPoolExecutor(threads,
                (t) -> new Thread(t, poolName + ranker.getAndIncrement()),
                new ChuuRejector(poolName));
    }


    public static ExecutorService of(String poolName) {
        return tpe;
//        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().uncaughtExceptionHandler((t, e) -> log.warn(e.getMessage(), e))
//                .inheritInheritableThreadLocals(false)
//                .name(poolName + "-Virtual-", 0).factory());
    }


}
