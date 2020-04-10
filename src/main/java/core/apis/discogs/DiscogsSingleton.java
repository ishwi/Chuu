package core.apis.discogs;

public class DiscogsSingleton {
    private static volatile DiscogsApi instance;
    private static String secret;
    private static String clientID;

    private DiscogsSingleton() {
    }

    //Not pretty
    public static void init(String secret2, String key2) {
        secret = secret2;
        clientID = key2;

    }


    public static DiscogsApi getInstanceUsingDoubleLocking() {
        if (instance == null) {
            synchronized (core.apis.discogs.DiscogsSingleton.class) {
                if (instance == null) {
                    instance = new DiscogsApi(secret, clientID);
                }
            }
        }
        return instance;
    }


}