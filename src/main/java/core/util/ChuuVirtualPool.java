package core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ChuuVirtualPool {

    private static final Logger log = LoggerFactory.getLogger(ChuuVirtualPool.class);


    public static ScheduledExecutorService ofScheduled(String poolName) {
        return new ScheduledThreadPoolExecutor(1,
                Thread.ofVirtual()
                        .uncaughtExceptionHandler((t, e) -> log.warn(e.getMessage(), e))
                        .name("Scheduled-" + poolName + "-Virtual", 0).factory(),
                new ChuuRejector(poolName));
    }

    public static ExecutorService of(String poolName) {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().uncaughtExceptionHandler((t, e) -> log.warn(e.getMessage(), e)).name(poolName + "-Virtual-", 0).factory());
    }


}
