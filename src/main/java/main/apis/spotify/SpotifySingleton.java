package main.apis.spotify;

public class SpotifySingleton {

	private static volatile Spotify instance;
	private static String secret;
	private static String clientID;

	//Not pretty
	public SpotifySingleton(String secret2, String clientID2) {
		secret = secret2;
		clientID = clientID2;
	}


	public static Spotify getInstanceUsingDoubleLocking() {
		if (instance == null) {
			synchronized (main.apis.spotify.SpotifySingleton.class) {
				if (instance == null) {
					instance = new Spotify(secret, clientID);
				}
			}
		}
		return instance;
	}

}