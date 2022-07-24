package core.util;

import core.Chuu;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class ChuuRejector implements RejectedExecutionHandler {

    public static final ExecutorService REJECTIONS = ChuuVirtualPool.of("rejections");

    public static final AtomicInteger exceedingTasks = new AtomicInteger(0);
    private final String poolName;

    public ChuuRejector(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        Chuu.getLogger().info("Rejected thread: Calling on virtual pool: {} currently active ", exceedingTasks);
        REJECTIONS.execute(() -> {
            try {
                exceedingTasks.incrementAndGet();
                r.run();
            } finally {
                exceedingTasks.decrementAndGet();
            }
        });
    }
}
