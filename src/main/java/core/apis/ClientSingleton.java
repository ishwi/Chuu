package core.apis;


import java.net.http.HttpClient;
import java.util.concurrent.Executors;

public class ClientSingleton {


    private ClientSingleton() {
    }

    public static synchronized HttpClient getInstance() {

        //HTTP/2 priority
        return InstanceHolder.instance;
    }

    private static final class InstanceHolder {
        private static final HttpClient instance = HttpClient.newBuilder()
                .executor(Executors.newCachedThreadPool())
                .priority(1) //HTTP/2 priority
                .version(HttpClient.Version.HTTP_2)
                .build();
    }
}
