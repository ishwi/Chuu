package core.apis;

import core.util.ChuuVirtualPool;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

public class ExecutorsSingleton {

    //    private static final AtomicInteger counter = new AtomicInteger(0);
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
    private static ExecutorService generateInstance() {
        return ChuuVirtualPool.of("Chuu-excutor");
//        ThreadPoolExecutor te = new ThreadPoolExecutor(
//                50,
//                50,
//                30,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(),
//                r -> new Thread(r, "Chuu-executor-" + counter.getAndIncrement()),
//                new ChuuRejector("Command-Pool")
//        );
//        te.allowCoreThreadTimeOut(true);

//        return te;
    }

}
