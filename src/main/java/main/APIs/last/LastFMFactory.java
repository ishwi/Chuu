package main.APIs.last;

import java.io.InputStream;
import java.util.Properties;

public class LastFMFactory {


	private LastFMFactory() {

	}

	public static ConcurrentLastFM getNewInstance() {
		Properties properties = new Properties();
		try (InputStream in = LastFMFactory.class.getResourceAsStream("/" + "all.properties")) {
			properties.load(in);
			String apikey = properties.getProperty("LASTFM_APIKEY");
			return new ConcurrentLastFM(apikey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}