package main.apis.last;

import dao.entities.*;
import main.Chuu;
import main.apis.ClientSingleton;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFMServiceException;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ConcurrentLastFM {//implements LastFMService {
	private final static String BASE = "http://ws.audioscrobbler.com/2.0/";
	private final static String GET_ALBUMS = "?method=user.gettopalbums&user=";
	private final static String GET_LIBRARY = "?method=library.getartists&user=";
	private final static String GET_USER = "?method=user.getinfo&user=";
	private final static String ENDING = "&format=json";
	private final static String RECENT_TRACKS = "?method=user.getrecenttracks";
	private final static String GET_NOW_PLAYINH = RECENT_TRACKS + "&limit=1&user=";
	private final static String GET_ALL = RECENT_TRACKS + "&limit=200&user=";
	private final static String GET_ARTIST = "?method=user.gettopartists&user=";
	private final static String GET_TRACKS = "?method=album.getinfo&username=";
	private final static String GET_TRACK_INFO = "?method=track.getInfo&username=";
	private final static String GET_TOP_TRACKS = "?method=user.gettoptracks&user=";
	private final static String GET_CORRECTION = "?method=artist.getcorrection&artist=";
	private final static String GET_ARTIST_ALBUMS = "?method=artist.gettopalbums&artist=";
	private final static String GET_ARTIST_INFO = "?method=artist.getinfo&artist=";
	private final static Header header = initHeader();
	private final String API_KEY;
	private final HttpClient client;

	public ConcurrentLastFM(String apikey) {
		this.API_KEY = "&api_key=" + apikey;
		this.client = ClientSingleton.getInstance();

	}

	private static Header initHeader() {
		Header h = new Header();
		h.setName("User-Agent");
		h.setValue("discordBot/ishwi6@gmail.com");
		return h;
	}

	//@Override
	public NowPlayingArtist getNowPlayingInfo(String user) throws LastFmException {
		String url = BASE + GET_NOW_PLAYINH + user + API_KEY + ENDING;
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

	private JSONObject doMethod(HttpMethod method) throws LastFmException {
		method.addRequestHeader(header);

		int counter = 0;
		while (true) {
			try {

				int response_code = client.executeMethod(method);
				parseHttpCode(response_code);
				byte[] responseBody = method.getResponseBody();
				return new JSONObject(new String(responseBody, StandardCharsets.UTF_8));

			} catch (IOException | LastFMServiceException e) {
				Chuu.getLogger().warn(e.getMessage(), e);
				//	throw new LastFMServiceException("HTTP");
			}//	throw new LastFMServiceException("IO");
			finally {
				method.releaseConnection();
			}
			System.out.println("Reattempting request");
			if (++counter == 3) {
				throw new LastFMServiceException("IO");
			}

		}
	}

	private void parseHttpCode(int code) throws LastFmException {
		if (code / 100 == 2)
			return;
		if (code == 404)
			throw new LastFmEntityNotFoundException("404");
		if (code == 500)
			throw new LastFMServiceException("500");

	}

	private HttpMethodBase createMethod(String url) {
		GetMethod method = new GetMethod(url);
		method.setRequestHeader(new Header("User-Agent", "IshDiscordBot"));
		return method;

	}

	//@Override
	public List<UserInfo> getUserInfo(List<String> lastFmNames) throws LastFmException {
		List<UserInfo> returnList = new ArrayList<>();

		for (String lastFmName : lastFmNames) {
			String url = BASE + GET_USER + lastFmName + API_KEY + ENDING;
			HttpMethodBase method = createMethod(url);
			JSONObject obj = doMethod(method);
			obj = obj.getJSONObject("user");
			JSONArray image = obj.getJSONArray("image");
			JSONObject bigImage = image.getJSONObject(image.length() - 1);
			String image2 = bigImage.getString("#text");
			int unixTime = obj.getJSONObject("registered").getInt("#text");
			int playCount = obj.getInt("playcount");
			returnList.add(new UserInfo(playCount, image2, lastFmName, unixTime));

		}

		return returnList;

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
		String url = BASE + apiMethod + userName + API_KEY + ENDING + "&period=" + weekly;

		int requestedSize = x * y;
		int size = 0;
		int page = 1;

		if (requestedSize > 700)
			url += "&limit=500";
		else if (requestedSize > 150)
			url += "&limit=200";

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
			if (limit == 0) {
				throw new LastFMNoPlaysException("");
			}
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
		int plays = albumObj.getInt("playcount");
		JSONObject bigImage = image.getJSONObject(image.length() - 1);

		return new UrlCapsule(bigImage.getString("#text"), size, albumName, artistName, mbid, plays);

	}

	private UrlCapsule parseArtist(JSONObject artistObj, int size) {
		String artistName = artistObj.getString("name");
		String mbid = artistObj.getString("mbid");
		int plays = artistObj.getInt("playcount");

//		JSONArray image = artistObj.getJSONArray("image");
//		JSONObject bigImage = image.getJSONObject(image.length() - 1);
		return new UrlCapsule(null, size, "", artistName, mbid, plays);
	}

	public SecondsTimeFrameCount getMinutesWastedOnMusic(String username, String period) throws LastFmException {
		String url = BASE + GET_TOP_TRACKS + username + API_KEY + ENDING + "&period=" + period + "&limit=500";
		int SONG_AVERAGE_LENGTH_SECONDS = 200;
		int page = 1;
		int total = 1;
		SecondsTimeFrameCount returned = new SecondsTimeFrameCount(TimeFrameEnum.fromCompletePeriod(period));
		int count = 0;
		int seconds = 0;
		while (page <= total) {
			HttpMethodBase method = createMethod(url + "&page=" + (page));
			System.out.println("Iteration :(");

			// Execute the method.
			JSONObject obj = doMethod(method);
			if (!obj.has("toptracks")) {
				throw new LastFmEntityNotFoundException(username);
			}

			obj = obj.getJSONObject("toptracks");
			if (page == 1) {
				total = obj.getJSONObject("@attr").getInt("totalPages");
				count = obj.getJSONObject("@attr").getInt("total");
			}
			++page;

			JSONArray arr = obj.getJSONArray("track");
			seconds += StreamSupport.stream(arr.spliterator(), false).mapToInt(x -> {
				JSONObject jsonObj = ((JSONObject) x);
				int duration = jsonObj.getInt("duration");
				int frequency = jsonObj.getInt("playcount");
				return duration == 0 ? SONG_AVERAGE_LENGTH_SECONDS * frequency : duration * frequency;
			}).sum();


		}
		returned.setCount(count);
		returned.setSeconds(seconds);
		System.out.println(count);
		return returned;

	}


	public Map<Track, Integer> getDurationsFromWeek(String user) throws LastFmException {
		Map<Track, Integer> trackList = new HashMap<>();
		String url = BASE + GET_TOP_TRACKS + user + API_KEY + ENDING + "&period=" + TimeFrameEnum.WEEK
				.toApiFormat() + "&limit=500";
		int SONG_AVERAGE_LENGTH_SECONDS = 200;
		int page = 1;
		int total = 1;
		while (page <= total) {
			HttpMethodBase method = createMethod(url + "&page=" + (page));
			System.out.println("Iteration :(");

			// Execute the method.
			JSONObject obj = doMethod(method);
			if (!obj.has("toptracks")) {
				throw new LastFmEntityNotFoundException(user);
			}

			obj = obj.getJSONObject("toptracks");
			if (page == 1) {
				total = obj.getJSONObject("@attr").getInt("totalPages");
			}
			++page;

			JSONArray arr = obj.getJSONArray("track");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject jsonObj = (arr.getJSONObject(i));
				String name = jsonObj.getString("name");
				int duration = jsonObj.getInt("duration");
				int frequency = jsonObj.getInt("playcount");
				duration = duration == 0 ? SONG_AVERAGE_LENGTH_SECONDS : duration;
				String artist_name = jsonObj.getJSONObject("artist").getString("name");

				trackList.put(new Track(artist_name, name, 0, false, 0), duration);
			}

		}
		return trackList;

	}

	public SecondsTimeFrameCount getMinutesWastedOnMusicDaily(String username, Map<Track, Integer> trackList, int timestampQuery) throws LastFmException {
		List<NowPlayingArtist> list = new ArrayList<>();
		String url = BASE + GET_ALL + username + API_KEY + ENDING + "&extended=1" + "&from=" + (timestampQuery + 1);
		SecondsTimeFrameCount returned = new SecondsTimeFrameCount(TimeFrameEnum.ALL);
		int count = 0;
		int seconds = 0;
		int page = 0;
		int total = 1;
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
					throw new LastFMNoPlaysException(username);
				}

				JSONArray arr = obj.getJSONArray("track");
				for (int i = 0; i < arr.length(); i++) {

					JSONObject trackObj = arr.getJSONObject(i);
					if (trackObj.has("@attr"))
						continue;
					String trackName = trackObj.getString("name");

					JSONObject artistObj = trackObj.getJSONObject("artist");

					String artistName = artistObj.getString("name");
					Track track = new Track(artistName, trackName, 0, false, 0);
					Integer duration;
					if ((duration = trackList.get(track)) != null) {
						seconds += duration;
						count++;
					}
				}
			} catch (JSONException e) {
				throw new LastFMNoPlaysException("a");
			}
		}
		returned.setCount(count);
		returned.setSeconds(seconds);
		return returned;
	}

	public TimestampWrapper<List<ArtistData>> getWhole(String user, int timestampQuery) throws LastFmException {
		List<NowPlayingArtist> list = new ArrayList<>();
		String url = BASE + GET_ALL + user + API_KEY + ENDING + "&extended=1";

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
		Map<String, Long> a = list.stream()
				.collect(Collectors.groupingBy(NowPlayingArtist::getArtistName, Collectors.counting()));
		return new TimestampWrapper<>(
				a.entrySet().stream().map(
						entry -> {
							String artist = entry.getKey();
							String tempUrl;
							Optional<NowPlayingArtist> r = list.stream()
									.filter(t -> t.getArtistName().equals(artist))
									.findAny();
							tempUrl = r.map(NowPlayingArtist::getUrl).orElse(null);
							return new
									ArtistData(entry.getKey(), entry.getValue().intValue(), tempUrl);
						})
						.collect(Collectors.toCollection(ArrayList::new)), timestamp);


	}

	public List<NowPlayingArtist> getRecent(String user, int limit) throws LastFmException {
		String url = BASE + RECENT_TRACKS + "&user=" + user + "&limit=" + limit + API_KEY + ENDING + "&extended=1";
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

	//@Override
	public List<ArtistData> getLibrary(String User) throws LastFmException {
		String url = BASE + GET_LIBRARY + User + API_KEY + ENDING;
		return getArtistDataTopAlbums(url, false);
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
				String topObject = isTopAlbums ? "topartists" : "artists";
				if (!obj.has(topObject))
					throw new LastFmEntityNotFoundException("a");
				obj = obj.getJSONObject(topObject);
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

	public String getCorrection(String artistToCorrect) {
		String url;
		url = BASE + GET_CORRECTION + URLEncoder.encode(artistToCorrect, StandardCharsets.UTF_8) + API_KEY + ENDING;
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

		JSONObject obj = initAlbumJSon(username, artist, album);
		artist = obj.getString("artist");
		album = obj.getString("name");

		JSONArray images = obj.getJSONArray("image");

		String image_url = images.getJSONObject(images.length() - 1).getString("#text");

		AlbumUserPlays ai = new AlbumUserPlays(album, image_url);
		ai.setPlays(obj.getInt("userplaycount"));
		ai.setArtist(artist);
		return ai;
	}

	private JSONObject initAlbumJSon(String username, String artist, String album) throws LastFmException {
		String url;
		url = BASE + GET_TRACKS + username + "&artist=" + URLEncoder
				.encode(artist, StandardCharsets.UTF_8) + "&album=" + URLEncoder.encode(album, StandardCharsets.UTF_8) +
				API_KEY + ENDING + "&autocorrect=1";

		HttpMethodBase method = createMethod(url);

		JSONObject obj = doMethod(method);
		if (!obj.has("album"))
			throw new LastFmEntityNotFoundException(artist);
		obj = obj.getJSONObject("album");
		return obj;
	}

	public FullAlbumEntity getTracksAlbum(String username, String artist, String album) throws
			LastFmException {
		JSONObject obj = initAlbumJSon(username, artist, album);

		JSONArray images = obj.getJSONArray("image");
		String correctedArtist = obj.getString("artist");
		String correctedAlbum = obj.getString("name");

		String image_url = images.getJSONObject(images.length() - 1).getString("#text");
		int playCount = obj.getInt("userplaycount");
		FullAlbumEntity fae = new FullAlbumEntity(correctedArtist, correctedAlbum, playCount, image_url, username);
		if (obj.has("tracks")) {
			JSONArray arr = obj.getJSONObject("tracks").getJSONArray("track");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject trackObj = arr.getJSONObject(i);
				String trackName = trackObj.getString("name");
				Track trackInfo = getTrackInfo(username, correctedArtist, trackName);
				trackInfo.setPosition(trackObj.getJSONObject("@attr").getInt("rank"));
				fae.addTrack(trackInfo);
			}

		} else {
			throw new LastFmEntityNotFoundException("No plays");
		}

		return fae;
	}

	public Track getTrackInfo(String username, String artist, String trackName) throws LastFmException {

		String url;
		url = BASE + GET_TRACK_INFO + username + "&artist=" + URLEncoder
				.encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder
				.encode(trackName, StandardCharsets.UTF_8) +
				API_KEY + ENDING + "&autocorrect=1";
		HttpMethodBase method = createMethod(url);

		JSONObject obj = doMethod(method);
		if (!obj.has("track"))
			throw new LastFmEntityNotFoundException(artist);
		obj = obj.getJSONObject("track");
		int userplaycount = obj.getInt("userplaycount");
		String re_trackName = obj.getString("name");
		boolean userloved = obj.getInt("userloved") != 0;
		int duration = obj.getInt("duration");
		String re_artist = obj.getJSONObject("artist").getString("name");

		Track track = new Track(re_artist, re_trackName, userplaycount, userloved, duration);

		JSONObject images;
		if ((images = obj).has("album") && (images = images.getJSONObject("album")).has("image")) {
			JSONArray ar = images.getJSONArray("image");
			track.setImageUrl(
					ar.getJSONObject(ar.length() - 1).getString("#text")
			);
		}
		return track;
	}

	public ArtistAlbums getAlbumsFromArtist(String artist, int topAlbums) throws
			LastFmException {

		List<AlbumUserPlays> albumList = new ArrayList<>(topAlbums);
		String url;
		String artistCorrected;

		url = BASE + GET_ARTIST_ALBUMS + URLEncoder
				.encode(artist, StandardCharsets.UTF_8) + API_KEY + "&autocorrect=1&limit=" + topAlbums + ENDING;

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

	public int getTotalAlbumCount(String username) throws LastFmException {
		String url = BASE + GET_ALBUMS + username + API_KEY + ENDING + "&period=overall&limit=1";
		HttpMethodBase method = createMethod(url);
		return doMethod(method).getJSONObject("topalbums").getJSONObject("@attr").getInt("total");

	}

	public List<Track> getTopArtistTracks(String username, String artist, String weekly) throws LastFmException {
		final int SIZE_LIMIT = 10;

		List<Track> trackList = new ArrayList<>();

		int artistPlays = getArtistPlays(artist, username);
		if (artistPlays == 0) {
			return trackList;
		}

		String url;
		int page = 1;
		boolean cont = true;
		boolean dontdoAll = true;

		int limit = 2;

		url = BASE + GET_TOP_TRACKS + username +
				API_KEY + "&limit=" + 1000 + ENDING + "&period=" + weekly;
		if (weekly.equalsIgnoreCase(TimeFrameEnum.WEEK.toApiFormat()) || weekly
				.equalsIgnoreCase(TimeFrameEnum.MONTH.toApiFormat())) {
			dontdoAll = false;
		}

		while (trackList.size() < SIZE_LIMIT && page < limit && cont) {

			String urlPage = url + "&page=" + page;
			HttpMethodBase method = createMethod(urlPage);

			++page;
			// Execute the method.
			JSONObject obj = doMethod(method);
			obj = obj.getJSONObject("toptracks");
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
			limit = obj.getJSONObject("@attr").getInt("totalPages");
			if (limit == 0) {
				throw new LastFMNoPlaysException("");
			}

			JSONArray arr = obj.getJSONArray("track");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject trackObj = arr.getJSONObject(i);
				int userplaycount = trackObj.getInt("playcount");
				if (dontdoAll && userplaycount < 3) {
					cont = false;
					break;
				}

				String artistName = trackObj.getJSONObject("artist").getString("name");

				if (artistName.equalsIgnoreCase(artist)) {
					String trackName = trackObj.getString("name");
					int duration = trackObj.getInt("duration");
					Track track = new Track(artist, trackName, userplaycount, false, duration);
					trackList.add(track);
					if (trackList.size() == SIZE_LIMIT)
						break;
				}

			}

		}
		return trackList;
	}

	private int getArtistPlays(String artist, String username) throws LastFmException {
		String url;
		url = BASE + GET_ARTIST_INFO + URLEncoder
				.encode(artist, StandardCharsets.UTF_8) + "&username=" + username + API_KEY + "&limit=" + 1000 + ENDING;
		HttpMethodBase method = createMethod(url);

		// Execute the method.

		JSONObject jsonObject = doMethod(method);

		if (jsonObject.has("artist")) {
			return jsonObject.getJSONObject("artist").getJSONObject("stats").getInt("userplaycount");
		} else
			return 0;

	}

	public ArtistSummary getArtistSummary(String artist, String username) throws LastFmException {
		String url;
		url = BASE + GET_ARTIST_INFO + URLEncoder
				.encode(artist, StandardCharsets.UTF_8) + "&username=" + username + API_KEY + "&limit=" + 1000 + ENDING;
		HttpMethodBase method = createMethod(url);
		// Execute the method.
		JSONObject jsonObject = doMethod(method);
		if (jsonObject.has("artist")) {
			JSONObject globalJson = jsonObject.getJSONObject("artist");
			JSONObject statObject = globalJson.getJSONObject("stats");
			int userPlayCount = statObject.getInt("userplaycount");
			int listeners = statObject.getInt("listeners");
			int playcount = statObject.getInt("playcount");

			JSONArray artistArray = globalJson.getJSONObject("similar").getJSONArray("artist");
			JSONArray tagsArray = globalJson.getJSONObject("tags").getJSONArray("tag");
			List<String> similars = StreamSupport.stream(artistArray.spliterator(), false).map(JSONObject.class::cast)
					.map(x -> x.getString("name")).collect(Collectors.toList());
			List<String> tags = StreamSupport.stream(tagsArray.spliterator(), false).map(JSONObject.class::cast)
					.map(x -> x.getString("name")).collect(Collectors.toList());

			String summary = globalJson.getJSONObject("bio").getString("summary");
			int i = summary.indexOf("<a");
			summary = summary.substring(0, i);

			return new ArtistSummary(userPlayCount, listeners, playcount, similars, tags, summary);
		} else
			return null;
	}


	public List<TimestampWrapper<Track>> getTracksAndTimestamps(String username, int from, int to) throws LastFmException {
		List<TimestampWrapper<Track>> list = new ArrayList<>();
		String url = BASE + GET_ALL + username + API_KEY + ENDING + "&extended=1" + "&from=" + (from) + "&to=" + to;

		int page = 0;
		int totalPages = 1;
		while (page < totalPages) {
			try {
				String urlPage = url + "&page=" + ++page;
				HttpMethodBase method = createMethod(urlPage);
				JSONObject obj = doMethod(method);
				obj = obj.getJSONObject("recenttracks");
				JSONObject attrObj = obj.getJSONObject("@attr");
				System.out.println("Plays " + attrObj.getInt("total"));
				totalPages = attrObj.getInt("totalPages");
				if (totalPages == 0) {
					throw new LastFMNoPlaysException(username);
				}

				JSONArray arr = obj.getJSONArray("track");
				for (int i = 0; i < arr.length(); i++) {

					JSONObject trackObj = arr.getJSONObject(i);
					if (trackObj.has("@attr"))
						continue;
					JSONObject date = trackObj.getJSONObject("date");
					int timestamp = date.getInt("uts");

					String trackName = trackObj.getString("name");

					JSONObject artistObj = trackObj.getJSONObject("artist");

					String artistName = artistObj.getString("name");
					Track track = new Track(artistName, trackName, 0, false, 0);
					TimestampWrapper<Track> wrapper = new TimestampWrapper<>(track, timestamp);
					list.add(wrapper);
				}
			} catch (JSONException e) {
				throw new LastFMNoPlaysException("a");
			}
		}
		return list;
	}

	public List<Track> getListTopTrack(String username, String timeframe) throws LastFmException {

		List<Track> trackList = new ArrayList<>();
		String url = BASE + GET_TOP_TRACKS + username + API_KEY + ENDING + "&period=" + timeframe + "&limit=200";
		HttpMethodBase method = createMethod(url);
		// Execute the method.
		JSONObject obj = doMethod(method);
		if (!obj.has("toptracks")) {
			throw new LastFmEntityNotFoundException(username);
		}

		obj = obj.getJSONObject("toptracks");
		if (obj.getJSONObject("@attr").getInt("totalPages") == 0)
			throw new LastFMNoPlaysException(username);

		JSONArray arr = obj.getJSONArray("track");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject jsonObj = (arr.getJSONObject(i));
			String name = jsonObj.getString("name");
			int duration = jsonObj.getInt("duration");
			int frequency = jsonObj.getInt("playcount");
			duration = duration == 0 ? 200 : duration;
			String artist_name = jsonObj.getJSONObject("artist").getString("name");

			trackList.add(new Track(artist_name, name, frequency, false, duration));
		}

		return trackList;

	}
}


