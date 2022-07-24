package core.apis;

import core.util.ChuuRejector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
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
            instance = generateInstance();
        }
        return instance;
    }

    @NotNull
    private static ThreadPoolExecutor generateInstance() {
        return new ThreadPoolExecutor(
                25,
                50,
                30,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                r -> new Thread(r, "Chuu-executor-" + counter.getAndIncrement()),
                new ChuuRejector("Command-Pool")
        );
    }

}
