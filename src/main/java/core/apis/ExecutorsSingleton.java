package core.apis;

import core.Chuu;
import core.util.ChuuRejector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorsSingleton {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final ReentrantLock lock = new ReentrantLock();
    private volatile static ExecutorService instance;

    private ExecutorsSingleton() {
    }


    /**
     * @return DO NOT HOLD THIS INSTANCE, CALL THE GETTER BEFORE ACCESSING ITS PARAMETERS
     */
    public static ExecutorService getInstance() {
        if (instance == null) {
            instance = generateInstance();
        }
        return instance;
    }

    public static void restartInstance() {
        lock.lock();
        try {
            if (instance == null)
                instance = generateInstance();
            else {
                try {
                    boolean b = instance.awaitTermination(5, TimeUnit.SECONDS);
                    Chuu.getLogger().info("Restarted Thread pool, Awaited: {}", b);
                } catch (InterruptedException e) {
                    Chuu.getLogger().warn(e.getMessage(), e);
                } finally {
                    instance = generateInstance();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @NotNull
    private static ThreadPoolExecutor generateInstance() {
        return new ThreadPoolExecutor(
                25,
                30,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(25),
                r -> new Thread(r, "Chuu-executor-" + counter.getAndIncrement()),
                new ChuuRejector("Command-Pool")
        );
    }

}
