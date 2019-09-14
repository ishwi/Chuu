package main.apis;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;

public class ClientSingleton {


	private static HttpClient instance;

	private ClientSingleton() {
	}

	public static synchronized HttpClient getInstance() {

		if (instance == null) {
			synchronized (main.apis.ClientSingleton.class) {
				if (instance == null) {
					HttpClient httpClient = new HttpClient();
					HttpClientParams params = new HttpClientParams();
					params.setSoTimeout(4000);
					params.setContentCharset("UTF-8");
					httpClient.setParams(params);
					httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
					instance = httpClient;
				}
			}
		}
		return instance;
	}
}