package core.apis.youtube;

public class SearchSingleton {
    private static Search instance;

    private SearchSingleton() {
    }

    public static synchronized Search getInstance() {
        if (instance == null) {
            instance = new Search();
        }
        return instance;
    }
}
