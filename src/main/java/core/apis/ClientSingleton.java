package core.apis;


import core.util.ChuuVirtualPool;

import java.net.http.HttpClient;

public class ClientSingleton {


    private ClientSingleton() {
    }

    public static HttpClient getInstance() {

        //HTTP/2 priority
        return InstanceHolder.instance;
    }



    private static final class InstanceHolder {

        private static final HttpClient instance = HttpClient.newBuilder()
                .executor(ChuuVirtualPool.of("Api-Executor"))
                .priority(1) //HTTP/2 priority
                .version(HttpClient.Version.HTTP_2)
                .build();

    }
}
