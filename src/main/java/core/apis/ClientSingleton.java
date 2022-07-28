package core.apis;


import java.net.http.HttpClient;
import java.time.Duration;

public class ClientSingleton {


    private ClientSingleton() {
    }

    public static HttpClient getInstance() {

        //HTTP/2 priority
        return InstanceHolder.instance;
    }


    private static final class InstanceHolder {

        private static final HttpClient instance = HttpClient.newBuilder()
                .priority(1) //HTTP/2 priority
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .build();

    }
}
