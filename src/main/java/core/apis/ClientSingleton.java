package core.apis;


import core.Chuu;

import java.net.http.HttpClient;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSingleton {


    private ClientSingleton() {
    }

    public static HttpClient getInstance() {

        //HTTP/2 priority
        return InstanceHolder.instance;
    }

    public static Executor getClientExecutor() {

        //HTTP/2 priority
        return InstanceHolder.executor;
    }

    private static final class InstanceHolder {
        private static final AtomicInteger ranker = new AtomicInteger(0);
        private static final Executor executor = new ThreadPoolExecutor(
                0
                , 10,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50),
                r -> new Thread(r, "Api-Executor" + ranker.getAndIncrement()),
                (r, executor) -> Chuu.getLogger().info(" Discarded thread: " + r.toString())
        );
        private static final HttpClient instance = HttpClient.newBuilder()
                .executor(executor)
                .priority(1) //HTTP/2 priority
                .version(HttpClient.Version.HTTP_2)
                .build();

    }
}
