package main.APIs.Discogs;

import main.Chuu;
import main.Exceptions.DiscogsServiceException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class DiscogsApiTest {

	@Test
	public void getYearRelease() {

		Properties properties = new Properties();
		try (InputStream in = DiscogsApiTest.class.getResourceAsStream("/" + "all.properties")) {
			properties.load(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}
		new DiscogsSingleton(properties.getProperty("DC_SC"), properties.getProperty("DC_KY"));
		DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		try {
			assertEquals(Year.of(2004), discogsApi.getYearRelease("恋人へ", "lamp"));
		} catch (DiscogsServiceException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}

	}
}