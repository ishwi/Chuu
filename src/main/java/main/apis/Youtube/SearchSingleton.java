package main.apis.Youtube;

public class SearchSingleton {

	private static volatile Search instance;


	public static Search getInstanceUsingDoubleLocking() {
		if (instance == null) {
			synchronized (main.apis.Youtube.Search.class) {
				if (instance == null) {
					instance = new Search();
				}
			}
		}
		return instance;


	}
}
