package main.last;

import DAO.Entities.ArtistData;
import DAO.Entities.NowPlayingArtist;
import DAO.Entities.UrlCapsule;
import DAO.Entities.UserInfo;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Youtube.DiscogsApi;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;


public class ConcurrentLastFM {//implements LastFMService {
	private final String API_KEY = "&api_key=***REMOVED***";
	private final String BASE = "http://ws.audioscrobbler.com/2.0/";
	private final String GET_ALBUMS = "?method=user.gettopalbums&user=";
	private final String GET_LIBRARY = "?method=library.getartists&user=";
	private final String GET_USER = "?method=user.getinfo&user=";
	private final String ending = "&format=json";
	private final String GET_NOW_PLAYINH = "?method=user.getrecenttracks&limit=1&user=";
	private final String GET_ALL = "?method=user.getrecenttracks&limit=200&user=";

	private final String GET_ARTIST = "?method=user.gettopartists&user=";
	private final String GET_TRACKS = "?method=user.getartisttracks&user=";
	private final String GET_CORRECTION = "?method=artist.getcorrection&artist=";
	private final HttpClient client;
	private final Header header;
	private DiscogsApi discogsApi;

	public ConcurrentLastFM() {
		this.client = ClientSingleton.getInstance();
		this.header = new Header();
		this.header.setName("User-Agent");
		this.header.setValue("discordBot/ishwi6@gmail.com");
	}

	public ConcurrentLastFM(DiscogsApi discogsApi) {
		this();
		this.discogsApi = discogsApi;

	}

	private void parseHttpCode(int code) throws LastFmException {
		if (code / 100 == 2)
			return;
		if (code == 404)
			throw new LastFmEntityNotFoundException("404");
		if (code == 500)
			throw new LastFMServiceException("500");

	}

	private JSONObject doMethod(HttpMethod method) throws LastFmException {
		method.addRequestHeader(this.header);
		try {

			int response_code = client.executeMethod(method);
			parseHttpCode(response_code);
			byte[] responseBody = method.getResponseBody();
			return new JSONObject(new String(responseBody));

		} catch (HttpException e) {
			System.out.println("HTTP");
			e.printStackTrace();
			throw new LastFMServiceException("HTTP");
		} catch (IOException e) {
			System.out.println("IO");
			e.printStackTrace();
			throw new LastFMServiceException("IO");


		} finally {

			method.releaseConnection();
		}


	}

	//@Override
	public NowPlayingArtist getNowPlayingInfo(String user) throws LastFmException {
		String url = BASE + GET_NOW_PLAYINH + user + API_KEY + ending;
		HttpMethodBase method = createMethod(url);

		JSONObject obj = doMethod(method);
		obj = obj.getJSONObject("recenttracks");
		JSONObject attrObj = obj.getJSONObject("@attr");
		if (attrObj.getInt("total") == 0) {
			throw new LastFMNoPlaysException(user);
		}
		boolean nowPlayin;


		JSONObject tracltObj = obj.getJSONArray("track").getJSONObject(0);

		try {
			nowPlayin = tracltObj.getJSONObject("@attr").getBoolean("nowplaying");
		} catch (JSONException e) {
			nowPlayin = false;
		}
		JSONObject artistObj = tracltObj.getJSONObject("artist");
		String artistname = artistObj.getString("#text");
		String mbid = artistObj.getString("mbid");

		String albumName = tracltObj.getJSONObject("album").getString("#text");
		String songName = tracltObj.getString("name");
		String image_url = tracltObj.getJSONArray("image").getJSONObject(2).getString("#text");

		return new NowPlayingArtist(artistname, mbid, nowPlayin, albumName, songName, image_url);


	}

	public TimestampWrapper<LinkedList<ArtistData>> getWhole(String user, int timestampQuery) throws LastFmException {
		List<NowPlayingArtist> list = new ArrayList<>();
		String url = BASE + GET_ALL + user + API_KEY + ending + "&extended=1";

		if (timestampQuery != 0)
			url += "&from=" + (timestampQuery + 1);
		int timestamp = 0;
		boolean catched = false;
		int page = 0;
		int total = 1;
		int count = 0;
		while (count < total) {
			try {
				String urlPage = url + "&page=" + ++page;
				HttpMethodBase method = createMethod(urlPage);
				JSONObject obj = doMethod(method);
				obj = obj.getJSONObject("recenttracks");
				JSONObject attrObj = obj.getJSONObject("@attr");
				System.out.println("Plays " + attrObj.getInt("total"));
				total = attrObj.getInt("total");
				if (total == 0) {
					throw new LastFMNoPlaysException(user);
				}


				JSONArray arr = obj.getJSONArray("track");
				for (int i = 0; i < arr.length(); i++) {
					JSONObject tracltObj = arr.getJSONObject(i);
					JSONObject artistObj = tracltObj.getJSONObject("artist");
					JSONArray images = artistObj.getJSONArray("image");
					//String image_url = images.getJSONObject(images.length() - 1).getString("#text");
					String image_url = tracltObj.getJSONArray("image").getJSONObject(images.length() - 1).getString("#text");

					String artistname = artistObj.getString("name");

					String albumName = tracltObj.getJSONObject("album").getString("#text");
					String songName = tracltObj.getString("name");
					if (!catched && tracltObj.has("date")) {
						timestamp = tracltObj.getJSONObject("date").getInt("uts");
						catched = true;
					}
					count++;
					list.add(new NowPlayingArtist(artistname, "", false, albumName, songName, image_url));
				}


			} catch (JSONException e) {
				throw new LastFMNoPlaysException("a");
			}
		}
		Map<String, Long> a = list.stream().collect(Collectors.groupingBy(NowPlayingArtist::getArtistName, Collectors.counting()));
		return new TimestampWrapper<>(
				a.entrySet().stream().map(
						entry -> {
							String artist = entry.getKey();
							String tempUrl;
							Optional<NowPlayingArtist> r = list.stream().filter(t -> t.getArtistName().equals(artist)).findAny();
							tempUrl = r.map(NowPlayingArtist::getUrl).orElse(null);
							return new
									ArtistData(entry.getKey(), entry.getValue().intValue(), tempUrl);
						})
						.collect(Collectors.toCollection(LinkedList::new)), timestamp);


	}

	//@Override
	public List<UserInfo> getUserInfo(List<String> lastFmNames) throws LastFmException {
		List<UserInfo> returnList = new ArrayList<>();


		for (String lastFmName : lastFmNames) {
			String url = BASE + GET_USER + lastFmName + API_KEY + ending;
			HttpMethodBase method = createMethod(url);
			JSONObject obj = doMethod(method);
			obj = obj.getJSONObject("user");
			JSONArray image = obj.getJSONArray("image");
			JSONObject bigImage = image.getJSONObject(2);
			String image2 = bigImage.getString("#text");
			int playcount = obj.getInt("playcount");
			returnList.add(new UserInfo(playcount, image2, lastFmName));

		}

		return returnList;

	}

	//@Override
	public LinkedList<ArtistData> getLibrary(String User) throws LastFmException {
		String url = BASE + GET_LIBRARY + User + API_KEY + ending;
		int page = 1;
		int pages = 1;
		url += "&limit=500";

		LinkedList<ArtistData> linkedlist = new LinkedList<>();
		while (page <= pages) {

			String urlPage = url + "&page=" + page;
			HttpMethodBase method = createMethod(urlPage);


			try {

				// Execute the method.
				JSONObject obj = doMethod(method);
				obj = obj.getJSONObject("artists");
				if (page++ == 1) {
					pages = obj.getJSONObject("@attr").getInt("totalPages");

				}

				JSONArray arr = obj.getJSONArray("artist");
				for (int i = 0; i < arr.length(); i++) {
					JSONObject artistObj = arr.getJSONObject(i);
					String mbid = artistObj.getString("name");

					int count = artistObj.getInt("playcount");
					JSONArray image = artistObj.getJSONArray("image");

					JSONObject bigImage = image.getJSONObject(image.length() - 1);

					linkedlist.add(new ArtistData(mbid, count, bigImage.getString("#text")));
				}
			} catch (JSONException e) {
				throw new LastFMNoPlaysException(e.getMessage());

			}
		}
		return linkedlist;
	}

	public void getUserList(String userName, String weekly, int x, int y, boolean isAlbum, BlockingQueue<UrlCapsule> queue) throws
			LastFmException {

		String apiMethod;
		String leadingObject;
		String arrayObject;
		if (isAlbum) {
			apiMethod = GET_ALBUMS;
			leadingObject = "topalbums";
			arrayObject = "album";
		} else {
			apiMethod = GET_ARTIST;
			leadingObject = "topartists";
			arrayObject = "artist";
		}
		String url = BASE + apiMethod + userName + API_KEY + ending + "&period=" + weekly;

		int requestedSize = x * y;
		int size = 0;
		int page = 1;

		if (requestedSize > 150)
			url += "&limit=500";
		int limit = requestedSize;
		while (size < requestedSize && size < limit) {

			String urlPage = url + "&page=" + page;
			HttpMethodBase method = createMethod(urlPage);

			++page;
			System.out.println(page + " :page             size: " + size);


			// Execute the method.
			JSONObject obj = doMethod(method);
			obj = obj.getJSONObject(leadingObject);
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
			limit = obj.getJSONObject("@attr").getInt("total");

			if (limit == size)
				break;
			JSONArray arr = obj.getJSONArray(arrayObject);
			for (int i = 0; i < arr.length() && size < requestedSize; i++) {
				JSONObject albumObj = arr.getJSONObject(i);
				if (isAlbum)
					queue.add(parseAlbum(albumObj, size));
				else
					queue.add(parseArtist(albumObj, size));

				++size;
			}

		}
		//
	}

	private UrlCapsule parseAlbum(JSONObject albumObj, int size) {
		JSONObject artistObj = albumObj.getJSONObject("artist");
		String albumName = albumObj.getString("name");
		String artistName = artistObj.getString("name");
		JSONArray image = albumObj.getJSONArray("image");
		JSONObject bigImage = image.getJSONObject(image.length() - 1);


		return new UrlCapsule(bigImage.getString("#text"), size, albumName, artistName);
	}

	private UrlCapsule parseArtist(JSONObject artistObj, int size) {
		String artistName = artistObj.getString("name");
		JSONArray image = artistObj.getJSONArray("image");
		JSONObject bigImage = image.getJSONObject(image.length() - 1);
		return new UrlCapsule(bigImage.getString("#text"), size, "", artistName);
	}

	private HttpMethodBase createMethod(String url) {
		GetMethod method = new GetMethod(url);
		method.setRequestHeader(new Header("User-Agent", "IshDiscordBot"));
		return method;

	}

	public String getCorrection(String artistToCorrect) throws LastFmException {
		String url;
		try {
			url = BASE + GET_CORRECTION + URLEncoder.encode(artistToCorrect, "UTF-8") + API_KEY + ending;
		} catch (UnsupportedEncodingException e) {
			throw new LastFMServiceException("500");
		}
		HttpMethodBase method = createMethod(url);
		JSONObject obj = doMethod(method);
		try {
			obj = obj.getJSONObject("corrections");
			JSONObject artistObj = obj.getJSONObject("correction").getJSONObject("artist");
			return artistObj.getString("name");
		} catch (JSONException e) {
			return artistToCorrect;
		}

	}

	public int getPlaysAlbum_Artist(String username, boolean isAlbum, String artist, String queriedString) throws
			LastFmException {

		int queryCounter = 0;
		String url;
		try {
			url = BASE + GET_TRACKS + username + "&artist=" + URLEncoder.encode(artist, "UTF-8") + API_KEY + ending + "&size=500";
		} catch (UnsupportedEncodingException e) {
			throw new LastFMServiceException("500");
		}
		int page = 1;
		while (true) {

			String urlPage = url + "&page=" + page;
			++page;

			HttpMethodBase method = createMethod(urlPage);

			System.out.println(page + " :page             size: ");

			JSONObject obj = doMethod(method);
			obj = obj.getJSONObject("artisttracks");


			JSONArray arr = obj.getJSONArray("track");
			int pageCounter = 0;
			for (int i = 0; i < arr.length(); i++) {
				JSONObject albumObj = arr.getJSONObject(i);
				if (!albumObj.has("date"))
					continue;

				if (isAlbum) {
					if (albumObj.getJSONObject("album").getString("#text").equalsIgnoreCase(queriedString))
						++queryCounter;
				} else if (albumObj.getString("name").equalsIgnoreCase(queriedString))
					++queryCounter;

				++pageCounter;
			}
			if (pageCounter != 50)
				break;


		}


		return queryCounter;
	}

}


