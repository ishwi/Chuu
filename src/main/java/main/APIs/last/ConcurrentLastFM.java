package main.APIs.last;

import DAO.Entities.*;
import main.APIs.ClientSingleton;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;


public class ConcurrentLastFM {//implements LastFMService {
	private final String API_KEY;
	private final String BASE = "http://ws.audioscrobbler.com/2.0/";
	private final String GET_ALBUMS = "?method=user.gettopalbums&user=";
	private final String GET_LIBRARY = "?method=library.getartists&user=";
	private final String GET_USER = "?method=user.getinfo&user=";
	private final String ending = "&format=json";
	private final String RECENT_TRACKS = "?method=user.getrecenttracks";
	private final String GET_NOW_PLAYINH = RECENT_TRACKS + "&limit=1&user=";
	private final String GET_ALL = RECENT_TRACKS + "&limit=200&user=";

	private final String GET_ARTIST = "?method=user.gettopartists&user=";
	private final String GET_TRACKS = "?method=album.getinfo&username=";
	private final String GET_CORRECTION = "?method=artist.getcorrection&artist=";
	private final String GET_ARTIST_ALBUMS = "?method=artist.gettopalbums&artist=";
	private final HttpClient client;
	private final Header header;

	public ConcurrentLastFM(String apikey) {
		this.API_KEY = "&api_key="+apikey;
		this.client = ClientSingleton.getInstance();
		this.header = new Header();
		this.header.setName("User-Agent");
		this.header.setValue("discordBot/ishwi6@gmail.com");
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
		boolean nowPlaying;


		JSONObject trackObj = obj.getJSONArray("track").getJSONObject(0);

		try {
			nowPlaying = trackObj.getJSONObject("@attr").getBoolean("nowplaying");
		} catch (JSONException e) {
			nowPlaying = false;
		}
		JSONObject artistObj = trackObj.getJSONObject("artist");
		String artistName = artistObj.getString("#text");
		String mbid = artistObj.getString("mbid");

		String albumName = trackObj.getJSONObject("album").getString("#text");
		String songName = trackObj.getString("name");
		String image_url = trackObj.getJSONArray("image").getJSONObject(2).getString("#text");

		return new NowPlayingArtist(artistName, mbid, nowPlaying, albumName, songName, image_url, user);


	}

	public List<NowPlayingArtist> getRecent(String user, int limit) throws LastFmException {
		String url = BASE + RECENT_TRACKS + "&user=" + user + "&limit=" + limit + API_KEY + ending + "&extended=1";
		HttpMethodBase method = createMethod(url);

		JSONObject obj = doMethod(method);
		if (!obj.has("recenttracks")) {
			throw new LastFmEntityNotFoundException(user);
		}

		obj = obj.getJSONObject("recenttracks");
		JSONObject attrObj = obj.getJSONObject("@attr");
		if (attrObj.getInt("total") == 0) {
			throw new LastFMNoPlaysException(user);
		}


		List<NowPlayingArtist> npList = new ArrayList<>();
		JSONArray arr = obj.getJSONArray("track");
		for (int i = 0; i < arr.length() && npList.size() < limit; i++) {
			JSONObject trackObj = arr.getJSONObject(i);
			JSONObject artistObj = trackObj.getJSONObject("artist");
			boolean np = false;
			if (trackObj.has("@attr"))
				np = true;
			//JSONArray images = artistObj.getJSONArray("image");
			//String image_url = images.getJSONObject(images.length() - 1).getString("#text");
			//String image_url = trackObj.getJSONArray("image").getJSONObject(images.length() - 1).getString("#text");

			String artistName = artistObj.getString("name");

			String albumName = trackObj.getJSONObject("album").getString("#text");
			String songName = trackObj.getString("name");
			String image_url = trackObj.getJSONArray("image").getJSONObject(2).getString("#text");

			npList.add(new NowPlayingArtist(artistName, "", np, albumName, songName, image_url, user));
		}
		return npList;
	}


	public TimestampWrapper<List<ArtistData>> getWhole(String user, int timestampQuery) throws LastFmException {
		List<NowPlayingArtist> list = new ArrayList<>();
		String url = BASE + GET_ALL + user + API_KEY + ending + "&extended=1";

		if (timestampQuery != 0)
			url += "&from=" + (timestampQuery + 1);
		int timestamp = 0;
		boolean caught = false;
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

					JSONObject trackObj = arr.getJSONObject(i);
					if (trackObj.has("@attr"))
						continue;
					JSONObject artistObj = trackObj.getJSONObject("artist");
					//JSONArray images = artistObj.getJSONArray("image");
					//String image_url = images.getJSONObject(images.length() - 1).getString("#text");
					//String image_url = trackObj.getJSONArray("image").getJSONObject(images.length() - 1).getString("#text");

					String artistName = artistObj.getString("name");

					String albumName = trackObj.getJSONObject("album").getString("#text");
					String songName = trackObj.getString("name");
					if (!caught && trackObj.has("date")) {
						timestamp = trackObj.getJSONObject("date").getInt("uts");
						caught = true;
					}
					count++;
					list.add(new NowPlayingArtist(artistName, "", false, albumName, songName, null, user));
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
						.collect(Collectors.toCollection(ArrayList::new)), timestamp);


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
			int playCount = obj.getInt("playcount");
			returnList.add(new UserInfo(playCount, image2, lastFmName));

		}

		return returnList;

	}

	//@Override
	public List<ArtistData> getLibrary(String User) throws LastFmException {
		String url = BASE + GET_LIBRARY + User + API_KEY + ending;
		return getArtistDataTopAlbums(url, false);
	}

	public List<ArtistData> getReducedLibrary(String user) throws LastFmException {
		String url = BASE + GET_ARTIST + user + API_KEY + ending + "&period=" + "7day";
		return getArtistDataTopAlbums(url, true);
	}

	@NotNull
	private List<ArtistData> getArtistDataTopAlbums(String url, boolean isTopAlbums) throws LastFmException {
		int page = 1;
		int pages = 1;
		url += "&limit=500";

		List<ArtistData> artistData = new ArrayList<>();
		while (page <= pages) {

			String urlPage = url + "&page=" + page;
			HttpMethodBase method = createMethod(urlPage);


			try {

				// Execute the method.
				JSONObject obj = doMethod(method);
				obj = isTopAlbums ? obj.getJSONObject("topartists") : obj.getJSONObject("artists");
				//obj = obj.getJSONObject("artists");
				if (page++ == 1) {
					pages = obj.getJSONObject("@attr").getInt("totalPages");

				}

				JSONArray arr = obj.getJSONArray("artist");
				for (int i = 0; i < arr.length(); i++) {
					JSONObject artistObj = arr.getJSONObject(i);
					String mbid = artistObj.getString("name");

					int count = artistObj.getInt("playcount");
//					JSONArray image = artistObj.getJSONArray("image");
//
//					JSONObject bigImage = image.getJSONObject(image.length() - 1);

					artistData.add(new ArtistData(mbid, count, null));
				}
			} catch (JSONException e) {
				throw new LastFMNoPlaysException(e.getMessage());

			}
		}
		return artistData;
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
		String mbid = albumObj.getString("mbid");

		JSONObject bigImage = image.getJSONObject(image.length() - 1);


		return new UrlCapsule(bigImage.getString("#text"), size, albumName, artistName, mbid);
	}

	private UrlCapsule parseArtist(JSONObject artistObj, int size) {
		String artistName = artistObj.getString("name");
		String mbid = artistObj.getString("mbid");

//		JSONArray image = artistObj.getJSONArray("image");
//		JSONObject bigImage = image.getJSONObject(image.length() - 1);
		return new UrlCapsule(null, size, "", artistName, mbid);
	}

	private HttpMethodBase createMethod(String url) {
		GetMethod method = new GetMethod(url);
		method.setRequestHeader(new Header("User-Agent", "IshDiscordBot"));
		return method;

	}

	public String getCorrection(String artistToCorrect) {
		String url;
		try {
			url = BASE + GET_CORRECTION + URLEncoder.encode(artistToCorrect, "UTF-8") + API_KEY + ending;
		} catch (UnsupportedEncodingException e) {
			return artistToCorrect;
		}
		int count = 0;
		while (true) {
			try {
				HttpMethodBase method = createMethod(url);
				JSONObject obj = doMethod(method);
				try {
					obj = obj.getJSONObject("corrections");
					JSONObject artistObj = obj.getJSONObject("correction").getJSONObject("artist");
					return artistObj.getString("name");
				} catch (JSONException e) {
					return artistToCorrect;
				}

			} catch (LastFmException e) {
				if (count++ == 4)
					return artistToCorrect;
			}
		}
	}

	public AlbumUserPlays getPlaysAlbum_Artist(String username, String artist, String album) throws
			LastFmException {
		String url;
		try {
			url = BASE + GET_TRACKS + username + "&artist=" + URLEncoder.encode(artist, "UTF-8") + "&album=" + URLEncoder.encode(album, "UTF-8") +
					API_KEY + ending + "&autocorrect=1";
		} catch (UnsupportedEncodingException e) {
			throw new LastFMServiceException("500");
		}


		HttpMethodBase method = createMethod(url);


		JSONObject obj = doMethod(method);
		if (!obj.has("album"))
			throw new LastFmEntityNotFoundException(artist);
		obj = obj.getJSONObject("album");
		JSONArray images = obj.getJSONArray("image");
		String image_url = images.getJSONObject(images.length() - 1).getString("#text");

		AlbumUserPlays ai = new AlbumUserPlays(album, image_url);
		ai.setPlays(obj.getInt("userplaycount"));
		return ai;
	}

	public ArtistAlbums getAlbumsFromArtist(String artist, int topAlbums) throws
			LastFmException {

		List<AlbumUserPlays> albumList = new ArrayList<>(topAlbums);
		String url;
		String artistCorrected;

		try {
			url = BASE + GET_ARTIST_ALBUMS + URLEncoder.encode(artist, "UTF-8") + API_KEY + "&autocorrect=1&limit=" + topAlbums + ending;
		} catch (UnsupportedEncodingException e) {
			throw new LastFMServiceException("500");
		}


		HttpMethodBase method = createMethod(url);


		JSONObject obj = doMethod(method);
		if (!obj.has("topalbums"))
			throw new LastFmEntityNotFoundException(artist);
		else {
			obj = obj.getJSONObject("topalbums");
			artistCorrected = obj.getJSONObject("@attr").getString("artist");
		}

		JSONArray arr = obj.getJSONArray("album");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject tempObj = arr.getJSONObject(i);
			JSONArray images = tempObj.getJSONArray("image");
			String image_url = images.getJSONObject(images.length() - 1).getString("#text");
			albumList.add(new AlbumUserPlays(tempObj.getString("name"), image_url));
		}

		return new ArtistAlbums(artistCorrected, albumList);
	}

}


