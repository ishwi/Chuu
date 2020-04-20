package core.apis.last;

import core.Chuu;
import core.apis.ClientSingleton;
import core.apis.last.exceptions.AlbumException;
import core.apis.last.exceptions.ArtistException;
import core.apis.last.exceptions.ExceptionEntity;
import core.apis.last.exceptions.TrackException;
import core.exceptions.*;
import dao.entities.*;
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
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ConcurrentLastFM {//implements LastFMService {
    static final String BASE = "http://ws.audioscrobbler.com/2.0/";
    static final String GET_ALBUMS = "?method=user.gettopalbums&user=";
    static final String GET_LIBRARY = "?method=library.getartists&user=";
    static final String GET_USER = "?method=user.getinfo&user=";
    static final String ENDING = "&format=json";
    static final String RECENT_TRACKS = "?method=user.getrecenttracks";
    static final String GET_NOW_PLAYINH = RECENT_TRACKS + "&limit=1&user=";
    static final String GET_ALL = RECENT_TRACKS + "&limit=200&user=";
    static final String GET_ARTIST = "?method=user.gettopartists&user=";
    static final String GET_TRACKS = "?method=album.getinfo&username=";
    static final String GET_TRACK_INFO = "?method=track.getInfo&username=";
    static final String GET_TOP_TRACKS = "?method=user.gettoptracks&user=";
    static final String GET_CORRECTION = "?method=artist.getcorrection&artist=";
    static final String GET_ARTIST_ALBUMS = "?method=artist.gettopalbums&artist=";
    static final String GET_ARTIST_INFO = "?method=artist.getinfo&artist=";
    static final Header header = initHeader();
    final String apiKey;
    final HttpClient client;

    public ConcurrentLastFM(String apikey) {
        this.apiKey = "&api_key=" + apikey;
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
        String url = BASE + GET_NOW_PLAYINH + user + apiKey + ENDING;
        JSONObject obj = initGetRecentTracks(user, url, TimeFrameEnum.ALL);
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
        String imageUrl = trackObj.getJSONArray("image").getJSONObject(2).getString("#text");

        return new NowPlayingArtist(artistName, mbid, nowPlaying, albumName, songName, imageUrl, user);


    }

    private JSONObject initGetRecentTracks(String user, String url, TimeFrameEnum timeFrameEnum) throws LastFmException {
        HttpMethodBase method = createMethod(url);
        JSONObject obj = doMethod(method, new ExceptionEntity(user));
        if (!obj.has("recenttracks")) {
            throw new LastFMNoPlaysException(user, timeFrameEnum.toApiFormat());
        }
        obj = obj.getJSONObject("recenttracks");
        JSONObject attrObj = obj.getJSONObject("@attr");
        if (attrObj.getInt("total") == 0) {
            throw new LastFMNoPlaysException(user, timeFrameEnum.toApiFormat());
        }
        return obj;
    }

    private JSONObject doMethod(HttpMethod method, ExceptionEntity causeOfNotFound) throws LastFmException {
        method.addRequestHeader(header);

        int counter = 0;
        while (true) {
            try {

                int responseCode = client.executeMethod(method);
                parseHttpCode(responseCode);
                byte[] responseBody = method.getResponseBody();
                JSONObject jsonObject = new JSONObject(new String(responseBody, StandardCharsets.UTF_8));
                if (jsonObject.has("error"))
                    parseResponse(jsonObject, causeOfNotFound);
                if (responseCode == 404) {
                    throw new LastFmEntityNotFoundException(new ExceptionEntity("Whatever"));
                }
                if (Math.floor((float) responseCode / 100) == 4) {
                    throw new UnknownLastFmException(jsonObject.toString(), responseCode);
                }
                return jsonObject;

            } catch (IOException | LastFMServiceException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage(), e);
            } finally {
                method.releaseConnection();
            }
            System.out.println("Reattempting request");
            if (++counter == 2) {
                throw new LastFMServiceException("500");
            }

        }
    }

    private void parseHttpCode(int code) throws LastFmException {
        if (code / 100 == 2)
            return;
        if (code == 500)
            throw new LastFMServiceException("500");

    }

    private void parseResponse(JSONObject jsonObject, ExceptionEntity exceptionEntity) throws LastFmEntityNotFoundException, UnknownLastFmException {
        int code = jsonObject.getInt("error");
        if (code == 6) {
            throw new LastFmEntityNotFoundException(exceptionEntity);
        } else throw new UnknownLastFmException(jsonObject.toString(), code);
    }

    private HttpMethodBase createMethod(String url) {
        return new GetMethod(url);

    }

    //@Override
    public List<UserInfo> getUserInfo(List<String> lastFmNames) throws LastFmException {
        List<UserInfo> returnList = new ArrayList<>();

        for (String lastFmName : lastFmNames) {
            String url = BASE + GET_USER + lastFmName + apiKey + ENDING;
            HttpMethodBase method = createMethod(url);
            JSONObject obj = doMethod(method, new ExceptionEntity(lastFmName));
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

    public List<ArtistInfo> getTopArtists(String userName, String weekly, int requestedSize) throws LastFmException {
        List<ArtistInfo> returnList = new ArrayList<>();
        int size = 0;
        String url = BASE + GET_ARTIST + userName + apiKey + ENDING + "&period=" + weekly;
        int page = 1;
        if (requestedSize >= 1000)
            url += "&limit=1000";
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
            JSONObject obj = doMethod(method, new ExceptionEntity(userName));
            obj = obj.getJSONObject("topartists");
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
            limit = obj.getJSONObject("@attr").getInt("total");
            if (limit == 0) {
                throw new LastFMNoPlaysException(userName, weekly);
            }
            if (limit == size)
                break;
            JSONArray arr = obj.getJSONArray("artist");
            for (int i = 0; i < arr.length() && size < requestedSize; i++) {
                JSONObject artistObj = arr.getJSONObject(i);
                String artistName = artistObj.getString("name");
                String mbid = artistObj.getString("mbid");
                returnList.add(new ArtistInfo(null, artistName, mbid));
                ++size;
            }
        }
        return returnList;
    }

    public List<AlbumInfo> getTopAlbums(String userName, String weekly, int requestedSize) throws LastFmException {
        List<AlbumInfo> returnList = new ArrayList<>();
        int size = 0;
        String url = BASE + GET_ALBUMS + userName + apiKey + ENDING + "&period=" + weekly;
        int page = 1;
        if (requestedSize >= 1000)
            url += "&limit=1000";
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

            JSONObject obj = doMethod(method, new ExceptionEntity(userName));
            obj = obj.getJSONObject("topalbums");
            limit = obj.getJSONObject("@attr").getInt("total");
            if (limit == 0) {
                throw new LastFMNoPlaysException(userName, weekly);
            }
            if (limit == size)
                break;
            JSONArray arr = obj.getJSONArray("album");
            for (int i = 0; i < arr.length() && size < requestedSize; i++) {
                JSONObject albumObj = arr.getJSONObject(i);
                JSONObject artistObj = albumObj.getJSONObject("artist");
                String albumName = albumObj.getString("name");
                String artistName = artistObj.getString("name");
                String mbid = albumObj.getString("mbid");
                returnList.add(new AlbumInfo(mbid, albumName, artistName));
                ++size;
            }
        }

        return returnList;
    }


    public int getChart(String userName, String weekly, int x, int y, TopEntity entity, BiFunction<JSONObject, Integer, UrlCapsule> parser, BlockingQueue<UrlCapsule> queue) throws
            LastFmException {

        String apiMethod = entity.getApiMethod();
        String leadingObject = entity.getLeadingObject();
        String arrayObject = entity.getArrayObject();

        String url = BASE + apiMethod + userName + apiKey + ENDING + "&period=" + weekly;

        int requestedSize = x * y;
        int size = 0;
        int page = 1;
        if (requestedSize >= 1000)
            url += "&limit=1000";
        if (requestedSize > 500)
            url += "&limit=500";
        else
            url += "&limit=" + requestedSize;

        int limit = requestedSize;
        while (size < requestedSize && size < limit) {

            String urlPage = url + "&page=" + page;
            if (page == 3) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Chuu.getLogger().warn(e.getMessage(), e);
                }
            }
            HttpMethodBase method = createMethod(urlPage);

            ++page;
            System.out.println(page + " :page             size: " + size);

            // Execute the method.
            JSONObject obj = doMethod(method, new ExceptionEntity(userName));
            obj = obj.getJSONObject(leadingObject);
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
            limit = obj.getJSONObject("@attr").getInt("total");
            if (limit == 0) {
                throw new LastFMNoPlaysException(userName, weekly);
            }
            if (limit == size)
                break;
            JSONArray arr = obj.getJSONArray(arrayObject);
            for (int i = 0; i < arr.length() && size < requestedSize; i++) {
                JSONObject albumObj = arr.getJSONObject(i);
                queue.add(parser.apply(albumObj, size));
                ++size;
            }
        }
        return limit;
    }


    public SecondsTimeFrameCount getMinutesWastedOnMusic(String username, String period) throws LastFmException {
        String url = BASE + GET_TOP_TRACKS + username + apiKey + ENDING + "&period=" + period + "&limit=500";
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
            JSONObject obj = doMethod(method, new ExceptionEntity(username));
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


    public Map<Track, Integer> getTrackDurations(String user, TimeFrameEnum timeFrameEnum) throws LastFmException {
        Map<Track, Integer> trackList = new HashMap<>();
        String url = BASE + GET_TOP_TRACKS + user + apiKey + ENDING + "&period=" + timeFrameEnum
                .toApiFormat() + "&limit=500";
        int SONG_AVERAGE_LENGTH_SECONDS = 200;
        int page = 1;
        int total = 1;
        boolean doWhole = timeFrameEnum.equals(TimeFrameEnum.WEEK);
        while (page <= total && (doWhole || page <= 2)) {
            HttpMethodBase method = createMethod(url + "&page=" + (page));
            System.out.println("Iteration :(");

            // Execute the method.
            JSONObject obj = doMethod(method, new ExceptionEntity(user));

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

    //TODO can do anythign with from and to timestamp
    public SecondsTimeFrameCount getMinutesWastedOnMusicDaily(String username, Map<Track, Integer> trackList, int timestampQuery) throws LastFmException {
        String url = BASE + GET_ALL + username + apiKey + ENDING + "&extended=1" + "&from=" + (timestampQuery + 1);
        SecondsTimeFrameCount returned = new SecondsTimeFrameCount(TimeFrameEnum.ALL);
        int count = 0;
        int seconds = 0;
        int page = 0;
        int total = 1;
        while (count < total) {
            String urlPage = url + "&page=" + ++page;
            JSONObject obj = initGetRecentTracks(username, urlPage, TimeFrameEnum.WEEK);
            JSONObject attrObj = obj.getJSONObject("@attr");
            total = attrObj.getInt("total");
            if (!obj.has("track")) {
                throw new LastFMNoPlaysException(username, TimeFrameEnum.WEEK.toApiFormat());
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

        }
        returned.setCount(count);
        returned.setSeconds(seconds);
        return returned;
    }

    public int getInfoPeriod(String username, int timestampQuery) throws LastFmException {
        String url = BASE + GET_NOW_PLAYINH + username + apiKey + ENDING + "&extended=1" + "&from=" + (timestampQuery);
        HttpMethodBase method = createMethod(url);
        JSONObject obj = doMethod(method, new ExceptionEntity(username));
        obj = obj.getJSONObject("recenttracks");
        JSONObject attrObj = obj.getJSONObject("@attr");
        return attrObj.getInt("total");
    }

    public StreakEntity getCombo(String username) throws LastFmException {
        String url = BASE + GET_ALL + username + apiKey + ENDING + "&extended=1";
        int page = 0;
        String currentArtist = null;
        String currentAlbum = null;
        String currentSong = null;
        int aCounter = 0;
        int albCounter = 0;
        int tCounter = 0;
        boolean inited = false;
        boolean stopAlbCounter = false;
        boolean stopArtistCounter = false;
        boolean stopTCounter = false;
        boolean cont = true;
        while (cont) {
            String urlPage = url + "&page=" + ++page;
            if (page == 6) {
                break;
            }
            JSONObject obj = initGetRecentTracks(username, urlPage, TimeFrameEnum.ALL);

            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject trackObj = arr.getJSONObject(i);
                String trackName = trackObj.getString("name");
                JSONObject artistObj = trackObj.getJSONObject("artist");
                String artistName = artistObj.getString("name");
                String albumString = null;
                JSONObject album = trackObj.getJSONObject("album");
                if (album.has("#text")) {
                    albumString = album.getString("#text");
                } else {
                    stopAlbCounter = true;
                }
                if (inited) {
                    if (!stopArtistCounter && currentArtist.equals(artistName)) {
                        aCounter++;
                    } else {
                        stopArtistCounter = true;
                    }
                    if (!stopAlbCounter && currentAlbum.equals(albumString)) {
                        albCounter++;
                    } else {
                        stopAlbCounter = true;
                        if (stopArtistCounter) {
                            cont = false;
                            break;
                        }
                    }
                    if (!stopTCounter && currentSong.equals(trackName))
                        tCounter++;
                    else
                        stopTCounter = true;
                } else {
                    if (!stopAlbCounter) {
                        albCounter++;
                        currentAlbum = albumString;
                    }

                    currentArtist = artistName;
                    currentSong = trackName;
                    aCounter++;
                    tCounter++;
                    inited = true;
                }
            }
        }
        return new StreakEntity(currentArtist, aCounter, currentAlbum, albCounter, currentSong, tCounter);
    }


    public TimestampWrapper<List<ScrobbledArtist>> getWhole(String username, int timestampQuery) throws LastFmException {
        List<NowPlayingArtist> list = new ArrayList<>();
        String url = BASE + GET_ALL + username + apiKey + ENDING + "&extended=1";

        if (timestampQuery != 0)
            url += "&from=" + (timestampQuery + 1);
        int timestamp = 0;
        boolean caught = false;
        int page = 0;
        int total = 1;
        int count = 0;
        while (count < total) {
            String urlPage = url + "&page=" + ++page;
            HttpMethodBase method = createMethod(urlPage);
            JSONObject obj = initGetRecentTracks(username, urlPage, TimeFrameEnum.ALL);

            JSONObject attrObj = obj.getJSONObject("@attr");
            total = attrObj.getInt("total");
            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {

                JSONObject trackObj = arr.getJSONObject(i);
                if (trackObj.has("@attr"))
                    continue;
                JSONObject artistObj = trackObj.getJSONObject("artist");

                String artistName = artistObj.getString("name");

                String albumName = trackObj.getJSONObject("album").getString("#text");
                String songName = trackObj.getString("name");
                if (!caught && trackObj.has("date")) {
                    timestamp = trackObj.getJSONObject("date").getInt("uts");
                    caught = true;
                }
                count++;
                list.add(new NowPlayingArtist(artistName, "", false, albumName, songName, null, username));
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
                                    ScrobbledArtist(entry.getKey(), entry.getValue().intValue(), tempUrl);
                        })
                        .collect(Collectors.toCollection(ArrayList::new)), timestamp);
    }

    public List<NowPlayingArtist> getRecent(String user, int limit) throws LastFmException {
        String url = BASE + RECENT_TRACKS + "&user=" + user + "&limit=" + limit + apiKey + ENDING + "&extended=1";
        JSONObject obj = initGetRecentTracks(user, url, TimeFrameEnum.ALL);
        List<NowPlayingArtist> npList = new ArrayList<>();
        JSONArray arr = obj.getJSONArray("track");

        for (int i = 0; i < arr.length() && npList.size() < limit; i++) {
            JSONObject trackObj = arr.getJSONObject(i);
            JSONObject artistObj = trackObj.getJSONObject("artist");

            boolean np = trackObj.has("@attr");

            String artistName = artistObj.getString("name");
            String albumName = trackObj.getJSONObject("album").getString("#text");
            String songName = trackObj.getString("name");
            String image_url = trackObj.getJSONArray("image").getJSONObject(2).getString("#text");

            npList.add(new NowPlayingArtist(artistName, "", np, albumName, songName, image_url, user));
        }
        return npList;
    }


    @NotNull
    public List<ScrobbledArtist> getAllArtists(String user, String period) throws LastFmException {
        String url = BASE + GET_ARTIST + user + apiKey + ENDING + "&period=" + period;

        int page = 1;
        int pages = 1;
        url += "&limit=500";

        List<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        while (page <= pages) {

            String urlPage = url + "&page=" + page;
            HttpMethodBase method = createMethod(urlPage);

            // Execute the method.
            JSONObject obj = doMethod(method, new ExceptionEntity(user));
            String topObject = "topartists";
            obj = obj.getJSONObject(topObject);
            if (page++ == 1) {
                pages = obj.getJSONObject("@attr").getInt("totalPages");
                if (obj.getJSONObject("@attr").getInt("total") == 0) {
                    throw new LastFMNoPlaysException(user);
                }
            }
            JSONArray arr = obj.getJSONArray("artist");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject artistObj = arr.getJSONObject(i);
                String artistName = artistObj.getString("name");
                int count = artistObj.getInt("playcount");
                scrobbledArtistData.add(new ScrobbledArtist(artistName, count, null));
            }
        }
        return scrobbledArtistData;
    }

    public String getCorrection(String artistToCorrect) throws LastFmException {
        String url;
        url = BASE + GET_CORRECTION + URLEncoder.encode(artistToCorrect, StandardCharsets.UTF_8) + apiKey + ENDING;
        HttpMethodBase method = createMethod(url);
        JSONObject obj = doMethod(method, new ArtistException(artistToCorrect));
        if (obj.has("corrections") && obj.get("corrections") instanceof String)
            throw new LastFmEntityNotFoundException(new ArtistException(artistToCorrect));

        obj = obj.getJSONObject("corrections");
        JSONObject artistObj = obj.getJSONObject("correction").getJSONObject("artist");
        return artistObj.getString("name");
    }

    public AlbumUserPlays getPlaysAlbumArtist(String userName, String artist, String album) throws
            LastFmException {

        JSONObject obj = initAlbumJSON(userName, artist, album);
        artist = obj.getString("artist");
        album = obj.getString("name");

        JSONArray images = obj.getJSONArray("image");

        String imageUrl = images.getJSONObject(images.length() - 1).getString("#text");

        AlbumUserPlays ai = new AlbumUserPlays(album, imageUrl);
        if (obj.has("userplaycount")) {
            ai.setPlays(obj.getInt("userplaycount"));
        } else {
            ai.setPlays(0);
        }
        ai.setArtist(artist);
        return ai;
    }

    private JSONObject initAlbumJSON(String username, String artist, String album) throws LastFmException {
        String url;
        url = BASE + GET_TRACKS + username + "&artist=" + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&album=" + URLEncoder.encode(album, StandardCharsets.UTF_8) +
              apiKey + ENDING + "&autocorrect=1";

        HttpMethodBase method = createMethod(url);
        JSONObject obj = doMethod(method, new AlbumException(artist, album));
        obj = obj.getJSONObject("album");
        return obj;
    }

    public FullAlbumEntity getTracksAlbum(String username, String artist, String album) throws
            LastFmException {
        JSONObject obj = initAlbumJSON(username, artist, album);

        JSONArray images = obj.getJSONArray("image");
        String correctedArtist = obj.getString("artist");
        String correctedAlbum = obj.getString("name");

        String imageUrl = images.getJSONObject(images.length() - 1).getString("#text");
        if (!obj.has("userplaycount")) {
            throw new LastFmEntityNotFoundException(new ExceptionEntity(username));
        }

        int playCount = obj.getInt("userplaycount");
        FullAlbumEntity fae = new FullAlbumEntity(correctedArtist, correctedAlbum, playCount, imageUrl, username);
        if (obj.has("tracks")) {
            JSONArray arr = obj.getJSONObject("tracks").getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject trackObj = arr.getJSONObject(i);
                String trackName = trackObj.getString("name");
                Track trackInfo = this.getTrackInfo(username, correctedArtist, trackName);
                trackInfo.setPosition(trackObj.getJSONObject("@attr").getInt("rank"));
                fae.addTrack(trackInfo);
            }
        }
        return fae;
    }

    public Track getTrackInfo(String username, String artist, String trackName) throws LastFmException {
        String url = BASE + GET_TRACK_INFO + username + "&artist=" + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder
                             .encode(trackName, StandardCharsets.UTF_8) +
                     apiKey + ENDING + "&autocorrect=1";
        HttpMethodBase method = createMethod(url);
        ExceptionEntity exceptionEntity = new TrackException(artist, trackName);
        JSONObject obj = doMethod(method, exceptionEntity);
        obj = obj.getJSONObject("track");
        int userplaycount = 0;
        if (obj.has("userplaycount")) {
            userplaycount = obj.getInt("userplaycount");
        }
        String reTrackName = obj.getString("name");
        boolean userloved = false;
        if (obj.has("userloved")) {
            userloved = obj.getInt("userloved") != 0;
        }
        int duration = obj.getInt("duration");
        String reArtist = obj.getJSONObject("artist").getString("name");

        Track track = new Track(reArtist, reTrackName, userplaycount, userloved, duration);

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
                .encode(artist, StandardCharsets.UTF_8) + apiKey + "&autocorrect=1&limit=" + topAlbums + ENDING;

        HttpMethodBase method = createMethod(url);

        JSONObject obj = doMethod(method, new ArtistException(artist));

        obj = obj.getJSONObject("topalbums");
        artistCorrected = obj.getJSONObject("@attr").getString("artist");

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
        String url = BASE + GET_ALBUMS + username + apiKey + ENDING + "&period=overall&limit=1";
        HttpMethodBase method = createMethod(url);
        return doMethod(method, new ExceptionEntity(username)).getJSONObject("topalbums").getJSONObject("@attr")
                .getInt("total");

    }

    public List<Track> getTopArtistTracks(String username, String artist, String weekly) throws LastFmException {
        final int SIZE_LIMIT = 10;

        List<Track> trackList = new ArrayList<>();

        int artistPlays = this.getArtistPlays(artist, username);
        if (artistPlays == 0) {
            return trackList;
        }

        String url;
        int page = 1;
        boolean cont = true;
        boolean dontdoAll = true;

        int limit = 2;

        url = BASE + GET_TOP_TRACKS + username +
              apiKey + "&limit=" + 1000 + ENDING + "&period=" + weekly;
        if (weekly.equalsIgnoreCase(TimeFrameEnum.WEEK.toApiFormat()) || weekly
                .equalsIgnoreCase(TimeFrameEnum.MONTH.toApiFormat())) {
            dontdoAll = false;
        }

        while (trackList.size() < SIZE_LIMIT && page < limit && cont) {

            String urlPage = url + "&page=" + page;
            HttpMethodBase method = createMethod(urlPage);

            ++page;
            // Execute the method.
            JSONObject obj = doMethod(method, new ExceptionEntity(username));
            obj = obj.getJSONObject("toptracks");
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
            limit = obj.getJSONObject("@attr").getInt("totalPages");
            if (limit == 0) {
                //TODO CHECK
                return trackList;
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
                .encode(artist, StandardCharsets.UTF_8) + "&username=" + username + apiKey + "&limit=" + 1000 + ENDING;
        HttpMethodBase method = createMethod(url);

        // Execute the method.

        JSONObject jsonObject = doMethod(method, new ArtistException(artist)).getJSONObject("artist");

        if (jsonObject.getJSONObject("stats").has("userplaycount")) {
            return jsonObject.getJSONObject("stats").getInt("userplaycount");
        } else
            return 0;

    }

    public ArtistSummary getArtistSummary(String artist, String username) throws LastFmException {
        String url;
        url = BASE + GET_ARTIST_INFO + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&username=" + username + apiKey + "&limit=" + 1000 + ENDING;
        HttpMethodBase method = createMethod(url);
        // Execute the method.
        JSONObject jsonObject = doMethod(method, new ArtistException(artist));
        JSONObject globalJson = jsonObject.getJSONObject("artist");
        JSONObject statObject = globalJson.getJSONObject("stats");
        int userPlayCount = statObject.getInt("userplaycount");
        int listeners = statObject.getInt("listeners");
        int playcount = statObject.getInt("playcount");
        String mbid = null;
        if (globalJson.has("mbid")) {
            mbid = globalJson.getString("mbid");
        }
        String artistName = globalJson.getString("name");
        JSONArray artistArray = globalJson.getJSONObject("similar").getJSONArray("artist");
        JSONArray tagsArray = globalJson.getJSONObject("tags").getJSONArray("tag");
        List<String> similars = StreamSupport.stream(artistArray.spliterator(), false).map(JSONObject.class::cast)
                .map(x -> x.getString("name")).collect(Collectors.toList());
        List<String> tags = StreamSupport.stream(tagsArray.spliterator(), false).map(JSONObject.class::cast)
                .map(x -> x.getString("name")).collect(Collectors.toList());

        String summary = globalJson.getJSONObject("bio").getString("summary");
        int i = summary.indexOf("<a");
        summary = summary.substring(0, i);

        return new ArtistSummary(userPlayCount, listeners, playcount, similars, tags, summary, artistName, mbid);

    }


    public List<TimestampWrapper<Track>> getTracksAndTimestamps(String username, int from, int to) throws LastFmException {
        List<TimestampWrapper<Track>> list = new ArrayList<>();
        String url = BASE + GET_ALL + username + apiKey + ENDING + "&extended=1" + "&from=" + (from) + "&to=" + to;

        int page = 0;
        int totalPages = 1;
        while (page < totalPages) {
            String urlPage = url + "&page=" + ++page;
            JSONObject obj = initGetRecentTracks(username, urlPage, TimeFrameEnum.WEEK);
            JSONObject attrObj = obj.getJSONObject("@attr");
            totalPages = attrObj.getInt("totalPages");
            if (totalPages == 0) {
                throw new LastFMNoPlaysException(username, TimeFrameEnum.WEEK.toApiFormat());
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

        }
        return list;
    }

    public List<Track> getListTopTrack(String username, String timeframe) throws LastFmException {

        List<Track> trackList = new ArrayList<>();
        String url = BASE + GET_TOP_TRACKS + username + apiKey + ENDING + "&period=" + timeframe + "&limit=200";
        HttpMethodBase method = createMethod(url);
        // Execute the method.
        JSONObject obj = doMethod(method, new ExceptionEntity(username));

        obj = obj.getJSONObject("toptracks");
        if (obj.getJSONObject("@attr").getInt("totalPages") == 0)
            throw new LastFMNoPlaysException(username, timeframe);

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

    public FullAlbumEntityExtended getAlbumSummary(String lastfmId, String artist, String album) throws LastFmException {
        JSONObject obj = initAlbumJSON(lastfmId, artist, album);

        JSONArray images = obj.getJSONArray("image");
        String correctedArtist = obj.getString("artist");
        String correctedAlbum = obj.getString("name");

        String image_url = images.getJSONObject(images.length() - 1).getString("#text");
        int playCount;
        if (!obj.has("userplaycount")) {
            playCount = 0;
        } else {
            playCount = obj.getInt("userplaycount");
        }
        int totalPlayCount = obj.getInt("playcount");
        int listeners = obj.getInt("listeners");

        int duration = 0;


        JSONArray tagsArray = obj.getJSONObject("tags").getJSONArray("tag");
        List<String> tags = StreamSupport.stream(tagsArray.spliterator(), false).map(JSONObject.class::cast)
                .map(x -> x.getString("name")).collect(Collectors.toList());
//TODO MBIZ
        FullAlbumEntityExtended fae = new FullAlbumEntityExtended(correctedArtist, correctedAlbum, playCount, image_url, lastfmId, listeners, totalPlayCount);
        fae.setTagList(tags);
        if (obj.has("tracks")) {
            JSONArray arr = obj.getJSONObject("tracks").getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject trackObj = arr.getJSONObject(i);
                String trackName = trackObj.getString("name");
                int duration1 = trackObj.getInt("duration");
                duration += duration1;
                Track track = new Track(correctedArtist, trackName, 0, false, duration1);
                track.setPosition(trackObj.getJSONObject("@attr").getInt("rank"));


                fae.addTrack(track);
            }
        }
        fae.setTotalDuration(duration);
        return fae;
    }

    public TrackExtended getTrackInfoExtended(String username, String artist, String song) throws LastFmException {
        String url = BASE + GET_TRACK_INFO + username + "&artist=" + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder
                             .encode(song, StandardCharsets.UTF_8) +
                     apiKey + ENDING + "&autocorrect=1";
        HttpMethodBase method = createMethod(url);
        ExceptionEntity exceptionEntity = new TrackException(artist, song);
        JSONObject obj = doMethod(method, exceptionEntity);
        obj = obj.getJSONObject("track");

        int userplaycount;
        if (!obj.has("userplaycount")) {
            userplaycount = 0;
        } else {
            userplaycount = obj.getInt("userplaycount");
        }
        int listeners = obj.getInt("listeners");
        int totalPlayCount = obj.getInt("playcount");


        String reTrackName = obj.getString("name");
        boolean userloved = obj.has("userlover") && obj.getInt("userloved") != 0;
        int duration = obj.getInt("duration") / 1000;
        String reArtist = obj.getJSONObject("artist").getString("name");
        JSONArray tagsArray = obj.getJSONObject("toptags").getJSONArray("tag");

        List<String> tags = StreamSupport.stream(tagsArray.spliterator(), false).map(JSONObject.class::cast)
                .map(x -> x.getString("name")).collect(Collectors.toList());
        String albumName = null;
        if ((obj).has("album")) {
            albumName = obj.getJSONObject("album").getString("title");
        }
        TrackExtended track = new TrackExtended(reArtist, reTrackName, userplaycount, userloved, duration, tags, totalPlayCount, listeners, albumName);

        JSONObject images;
        if ((images = obj).has("album") && (images = images.getJSONObject("album")).has("image")) {
            JSONArray ar = images.getJSONArray("image");
            track.setImageUrl(
                    ar.getJSONObject(ar.length() - 1).getString("#text")
            );
        }
        return track;
    }


    public int scrobblesSince(String username, OffsetDateTime date) throws LastFmException {
        long time = date.toEpochSecond();
        String url = BASE + GET_ALL + username + apiKey + ENDING + "&extended=0&limit=1&from=" + (time + 1);
        JSONObject obj = initGetRecentTracks(username, url, TimeFrameEnum.ALL);
        JSONObject attrObj = obj.getJSONObject("@attr");
        return attrObj.getInt("total");
    }
}


