package test.commands.utils;

public class TestResourcesSingleton {



	private static TestResources instance;

	private TestResourcesSingleton() {
	}

	public static synchronized TestResources getInstance() {
		if (instance == null) {
			instance = new TestResources();
		}
		return instance;
	}
}

