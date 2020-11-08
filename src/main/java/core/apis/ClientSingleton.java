package core.apis;


import java.net.http.HttpClient;
import java.util.concurrent.Executors;

public class ClientSingleton {


    private static HttpClient instance;

    private ClientSingleton() {
    }

    public static synchronized HttpClient getInstance() {

        if (instance == null) {
            synchronized (core.apis.ClientSingleton.class) {
                if (instance == null) {
                    instance = HttpClient.newBuilder()
                            .executor(Executors.newCachedThreadPool())
                            .priority(1) //HTTP/2 priority
                            .version(HttpClient.Version.HTTP_2)
                            .build();
                }
            }
        }
        return instance;
    }
}
