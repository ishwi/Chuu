package core.apis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsSingleton {
    private volatile static ExecutorService instance;

    private ExecutorsSingleton() {
    }


    public static ExecutorService getInstance() {
        if (instance == null) {
            instance = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().allowSetThreadLocals(false).inheritInheritableThreadLocals(false).name("Chuu-executor-", 0).factory());
        }
        return instance;

    }
}
