package main.APIs.Discogs;


import main.Chuu;
import main.Exceptions.DiscogsServiceException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DiscogsApi {
	private static final String BASE_API = "https://api.discogs.com/";
	private final String SECRET;
	private final String KEY;
	private final Header header;
	private final HttpClient httpClient;
	private boolean slowness = false;

	public DiscogsApi(String secret, String key) {
		this.KEY = key;
		this.SECRET = secret;
		this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		this.header = new Header();
		this.header.setName("User-Agent");
		this.header.setValue("discordArtistImageFetcher/ishwi6@gmail.com");

	}


	private int doSearch(String query) throws DiscogsServiceException {

		System.out.println("DOING SEARCH : " + query);
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}
		String url = BASE_API + "database/search?q=" + query + "&type=artist&key=" + KEY + "&secret=" + SECRET;

		GetMethod getMethod = new GetMethod(url);
		JSONObject obj = doMethod(httpClient, getMethod);

		JSONArray results = obj.getJSONArray("results");
		int id = 0;
		for (int i = 0; i < results.length(); i++) {
			JSONObject resultObj = results.getJSONObject(i);

			if (new org.apache.commons.text.similarity.LevenshteinDetailedDistance()
					.apply(query, resultObj.getString("title")).getDistance() < query.length() / 3) {
				id = resultObj.getInt("id");
				break;
			}

		}
		return id;
	}

	public Year getYearRelease(String album, String artist) throws DiscogsServiceException {
		String albumenc;
		String artistenc;
		try {
			albumenc = URLEncoder.encode(album, "UTF-8");
			artistenc = URLEncoder.encode(artist, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			return null;
		}

		String url = BASE_API + "database/search?&type=release&artist=" + artistenc + "&release_title=" + albumenc + "&key=" + KEY + "&secret=" + SECRET;
		GetMethod getMethod = new GetMethod(url);
		JSONObject obj = doMethod(httpClient, getMethod);
		JSONArray results = obj.getJSONArray("results");
		String expected = artist + " - " + album;
		for (int i = 0; i < results.length(); i++) {
			JSONObject resultObj = results.getJSONObject(i);

			if (new org.apache.commons.text.similarity.LevenshteinDetailedDistance().apply(
					expected.toLowerCase(), resultObj.getString("title").toLowerCase().replaceAll("\\s\\(\\d+\\)", ""))
					.getDistance() < expected.length() / 2) {

				if (resultObj.has("year"))
					return Year.of(resultObj.getInt("year"));
			}
		}
		return null;
	}

	private JSONObject doMethod(HttpClient httpClient, HttpMethod method) throws DiscogsServiceException {
		method.addRequestHeader(header);
		try {
			if (slowness) {
				TimeUnit.SECONDS.sleep(1);
				System.out.println("RATE LIMITED");
			}

			int response_code = httpClient.executeMethod(method);
			parseHttpCode(response_code);
			slowness = Integer.parseInt(method.getResponseHeader("X-Discogs-Ratelimit-Remaining").getValue()) == 0;
			byte[] responseBody = method.getResponseBody();
			return new JSONObject(new String(responseBody, StandardCharsets.UTF_8));

		} catch (IOException | InterruptedException e) {
			method.releaseConnection();
			throw new DiscogsServiceException(e.toString());
		}
	}

	private void parseHttpCode(int code) throws HttpResponseException {
		if (code / 100 == 2)
			return;
		if (code == 404)
			return;

		throw new HttpResponseException(code, "error on discogs service " + code);
	}

	private String doArtistInfo(int id) throws DiscogsServiceException {

		String imageUrl = null;

		if (id == 0)
			return "";
		String url = BASE_API + "artists/" + id + "?key=" + KEY + "&secret=" + SECRET;
		GetMethod getMethod = new GetMethod(url);
		JSONObject obj = doMethod(httpClient, getMethod);
		if (!obj.has("images"))
			return "";

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

			if (((height + width) / (Float.max(height, width) / Float
					.min(height, width))) <= ((height2 + width2) / (Float.max(height2, width2) / Float
					.min(height2, width2)))) {
				return -1;
			} else
				return 1;
		});
		if (opt.isPresent())
			imageUrl = opt.get().getString("resource_url");

		return imageUrl;
	}

	public String findArtistImage(String artist) throws DiscogsServiceException {
		return doArtistInfo(doSearch(artist));
	}
}