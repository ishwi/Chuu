package core.apis;

import core.Chuu;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorsSingleton {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private volatile static ExecutorService instance;

    private ExecutorsSingleton() {
    }


    public static ExecutorService getInstance() {
        if (instance == null) {

            instance = new ThreadPoolExecutor(
                    3,
                    300,
                    240L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(200),
                    r -> new Thread(r, "Chuu-executor-" + counter.getAndIncrement()),
                    (r, executor) -> Chuu.getLogger().info(" Discarded thread: " + r.toString())
            );
        }
        return instance;
    }

}
