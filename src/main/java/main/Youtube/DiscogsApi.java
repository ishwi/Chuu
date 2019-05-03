package main.Youtube;


import main.Exceptions.DiscogsServiceException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DiscogsApi {
	private final String BASE_API = "https://api.discogs.com/";
	private final String SECRET;
	private final String KEY;
	private final Header header;
	private boolean slowness = false;

	public DiscogsApi() {
		Properties properties = new Properties();
		try {
			properties.load(DiscogsApi.class.getResourceAsStream("/" + "all.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.header = new Header();
		this.header.setName("User-Agent");
		this.header.setValue("discordArtistImageFetcher/ishwi6@gmail.com");
		this.SECRET = properties.getProperty("DC_SC");
		this.KEY = properties.getProperty("DC_KY");

	}

	public static void main(String[] args) throws IOException {


	}

	public int doSearch(String query) throws DiscogsServiceException {
		HttpClient httpClient = new HttpClient();

		System.out.println("DOIUNG SERACH : " + query);
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = BASE_API + "database/search?q=" + query + "&type=artist&key=" + KEY + "&secret=" + SECRET;

		GetMethod getMethod = new GetMethod(url);
		JSONObject obj = doMethod(httpClient, getMethod);
		JSONArray results = obj.getJSONArray("results");
		int id = 0;
		for (int i = 0; i < results.length(); i++) {
			JSONObject resultObj = results.getJSONObject(i);

			if (new org.apache.commons.text.similarity.LevenshteinDetailedDistance().apply(query, resultObj.getString("title")).getDistance() < query.length() / 3) {
				id = resultObj.getInt("id");
				break;
			}

		}
		return id;
	}

	public String doArtistInfo(int id) throws DiscogsServiceException {
		HttpClient httpClient = new HttpClient();

		String imageUrl = null;

		if (id == 0)
			return "";
		String url = BASE_API + "/artists/" + id + "?key=" + KEY + "&secret=" + SECRET;
		GetMethod getMethod = new GetMethod(url);
		JSONObject obj = doMethod(httpClient, getMethod);

		JSONArray images = obj.getJSONArray("images");
		List<JSONObject> list = new ArrayList<>();
		for (int i = 0; i < images.length(); i++) {

			JSONObject resultObj = images.getJSONObject(i);
			list.add(resultObj);
			float height = resultObj.getInt("height");
			float width = resultObj.getInt("width");
			float a;
			if (((a = height / width) > 0.9f && a < 1.1f) && height >= 300) {
				return resultObj.getString("resource_url");
			}

//			if (a < best) {
//
//				imageUrl = resultObj.getString("resource_url");
//				best = a;
//			}

		}
		Optional<JSONObject> opt = list.stream().max((obj1, obj2) -> {
			float height = obj1.getInt("height");
			float width = obj1.getInt("width");
			float height2 = obj2.getInt("height");
			float width2 = obj2.getInt("width");

			if (((height + width) / (Float.max(height, width) / Float.min(height, width))) <= ((height2 + width2) / (Float.max(height2, width2) / Float.min(height2, width2)))) {
				return -1;
			} else
				return 1;
		});
		if (opt.isPresent())
			imageUrl = opt.get().getString("resource_url");

		return imageUrl;
	}

	private JSONObject doMethod(HttpClient httpClient, HttpMethod method) throws DiscogsServiceException {
		method.addRequestHeader(header);
		try {
			if (slowness)
				TimeUnit.SECONDS.sleep(1);

			int response_code = httpClient.executeMethod(method);
			parseHttpCode(response_code);
			slowness = Integer.parseInt(method.getResponseHeader("X-Discogs-Ratelimit-Remaining").getValue()) == 0;
			byte[] responseBody = method.getResponseBody();
			return new JSONObject(new String(responseBody));

		} catch (IOException | InterruptedException e) {
			method.releaseConnection();
			throw new DiscogsServiceException(e.getCause().toString());
		}
	}

	public String findArtistImage(String artist) throws DiscogsServiceException {
		return doArtistInfo(doSearch(artist));
	}

	private void parseHttpCode(int code) throws HttpResponseException {
		if (code / 100 == 2)
			return;

		throw new HttpResponseException(code, "error on discogs service");
	}
}