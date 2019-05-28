package main.APIs.Discogs;

public class DiscogsSingleton {

	private static DiscogsApi instance;
	private static String secret;
	private static String clientID;

	public DiscogsSingleton(String secret2, String key2) {
		secret = secret2;
		clientID = key2;

	}


	private static synchronized DiscogsApi initializeInstance() {
		instance = new DiscogsApi(secret, clientID);
		return instance;
	}

	public static synchronized DiscogsApi getInstance() {
		if (instance == null) {
			instance = initializeInstance();
		}
		return instance;
	}

	public static DiscogsApi getInstanceUsingDoubleLocking() {
		if (instance == null) {
			synchronized (main.APIs.Discogs.DiscogsSingleton.class) {
				if (instance == null) {
					instance = initializeInstance();
				}
			}
		}
		return instance;
	}

}