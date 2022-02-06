package core.util;

import core.Chuu;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ChuuRejector implements RejectedExecutionHandler {

    private final String poolName;

    public ChuuRejector(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        Chuu.getLogger().info("Queue was full {}  ", poolName);
        if (!e.isShutdown()) {
            e.getQueue().poll();
            e.execute(r);
        }
    }
}
