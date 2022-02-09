package core.apis;

import core.util.ChuuRejector;

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
                    15,
                    40,
                    30L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(40),
                    r -> new Thread(r, "Chuu-executor-" + counter.getAndIncrement()),
                    new ChuuRejector("Command-Pool")
            );
        }
        return instance;
    }

    public static ThreadPoolExecutor getCast() {
        return ((ThreadPoolExecutor) instance);
    }

}
