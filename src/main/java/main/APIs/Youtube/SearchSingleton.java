package main.APIs.Youtube;

public class SearchSingleton {

	private static Search instance;


	public static Search getInstanceUsingDoubleLocking() {
		if (instance == null) {
			synchronized (main.APIs.Youtube.Search.class) {
				if (instance == null) {
					instance = new Search();
				}
			}
		}
		return instance;


	}
}
