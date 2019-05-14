package main.APIs;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;

public class ClientSingleton {


	private static HttpClient instance;

	private ClientSingleton() {
	}

	public static synchronized HttpClient getInstance() {
		if (instance == null) {
			instance = new HttpClient();
			HttpClientParams params = new HttpClientParams();
			params.setSoTimeout(4000);
			params.setContentCharset("UTF-8");
			instance.setParams(params);

			instance.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
		}
		return instance;
	}

	public static HttpClient getInstanceUsingDoubleLocking() {
		if (instance == null) {
			synchronized (ClientSingleton.class) {
				if (instance == null) {
					instance = new HttpClient();
					HttpClientParams params = new HttpClientParams();
					params.setSoTimeout(4000);
					params.setContentCharset("UTF-8");
					instance.setParams(params);

					instance.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());

				}
			}
		}
		return instance;
	}

}