package core.apis.spotify;

public class SpotifySingleton {
    private static Spotify instance;
    private static String secret;
    private static String clientID;

    private SpotifySingleton() {
    }

    //Not pretty
    public static void init(String secret2, String clientID2) {
        secret = secret2;
        clientID = clientID2;
    }


    public static synchronized Spotify getInstance() {
        if (instance == null) {
            instance = new Spotify(secret, clientID);
        }
        return instance;
    }

}