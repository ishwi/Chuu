package core.apis;

import core.util.ChuuVirtualPool;

import java.util.concurrent.ExecutorService;

public class ExecutorsSingleton {
    private volatile static ExecutorService instance;

    private ExecutorsSingleton() {
    }


    public static ExecutorService getInstance() {
        if (instance == null) {
            instance = ChuuVirtualPool.of("Chuu-executor");
        }
        return instance;

    }
}
