package main.APIs.Spotify;

public class SpotifySingleton {

	private static Spotify instance;
	private static String secret;
	private static String clientID;

	public SpotifySingleton(String secret2, String clientID2) {
		secret = secret2;
		clientID = clientID2;
	}

	public static synchronized Spotify getInstance() {
		if (instance == null) {
			instance = initializeInstance();
		}
		return instance;
	}

	private static synchronized Spotify initializeInstance() {
		instance = new Spotify(secret, clientID);
		return instance;
	}

	public static Spotify getInstanceUsingDoubleLocking() {
		if (instance == null) {
			synchronized (main.APIs.Spotify.SpotifySingleton.class) {
				if (instance == null) {
					instance = initializeInstance();
				}
			}
		}
		return instance;
	}

}