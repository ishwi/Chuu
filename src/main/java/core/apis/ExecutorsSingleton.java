package core.apis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsSingleton {


    private static ExecutorService instance;

    private ExecutorsSingleton() {
    }


    public static synchronized ExecutorService getInstance() {
        if (instance == null) {
            instance = Executors.newCachedThreadPool();
        }
        return instance;
    }

}