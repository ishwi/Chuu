package core.apis;

import core.util.ChuuRejector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
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
        ThreadPoolExecutor te = new ThreadPoolExecutor(
                50,
                50,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "Chuu-executor-" + counter.getAndIncrement()),
                new ChuuRejector("Command-Pool")
        );
        te.allowCoreThreadTimeOut(true);

        return te;
    }

}
