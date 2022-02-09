package core.util;

import core.Chuu;
import core.commands.utils.CommandUtil;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ChuuRejector implements RejectedExecutionHandler {

    private final String poolName;

    public ChuuRejector(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        Chuu.getLogger().info("Queue was full {} | Executor Status {} | Size {}", poolName, e.isShutdown(), e.getQueue().size());
        if (!e.isShutdown()) {
            if (CommandUtil.rand.nextBoolean()) {
                e.getQueue().removeIf(t -> true);
                Chuu.getLogger().info("Clearing task queue {} ", e.getQueue().size());
            } else {
                e.execute(r);
            }
        }
    }
}
