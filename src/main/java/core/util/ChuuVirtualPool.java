package core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ChuuVirtualPool {


    public static ScheduledExecutorService ofScheduled(String poolName) {
        return new ScheduledThreadPoolExecutor(1,
                Thread.ofVirtual().name("Scheduled-" + poolName + "-Virtual", 0).factory(),
                new ChuuRejector(poolName));
    }

    public static ExecutorService of(String poolName) {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(poolName + "-Virtual-", 0).factory());
    }


}
