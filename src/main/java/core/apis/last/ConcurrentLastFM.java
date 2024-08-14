package core.apis.last;

import core.Chuu;
import core.apis.ClientSingleton;
import core.apis.last.entities.LovePost;
import core.apis.last.entities.PostEntity;
import core.apis.last.entities.Scrobble;
import core.apis.last.entities.ScrobblePost;
import core.apis.last.entities.TrackExtended;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.TrackDurationChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.exceptions.AlbumException;
import core.apis.last.exceptions.ArtistException;
import core.apis.last.exceptions.ExceptionEntity;
import core.apis.last.exceptions.TrackException;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFMConnectionException;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFMServiceException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import core.exceptions.UnknownLastFmException;
import core.parsers.params.ChartParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.NPService;
import core.services.OAuthService;
import core.util.UniqueBag;
import core.util.VirtualParallel;
import dao.entities.AlbumInfo;
import dao.entities.AlbumUserPlays;
import dao.entities.ArtistAlbums;
import dao.entities.ArtistInfo;
import dao.entities.ArtistSummary;
import dao.entities.CountWrapper;
import dao.entities.FullAlbumEntity;
import dao.entities.FullAlbumEntityExtended;
import dao.entities.Genre;
import dao.entities.GenreInfo;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.StreakEntity;
import dao.entities.TimeFrameEnum;
import dao.entities.TimestampWrapper;
import dao.entities.Track;
import dao.entities.TrackWithArtistId;
import dao.entities.TriFunction;
import dao.entities.UserInfo;
import dao.exceptions.ChuuServiceException;
import org.apache.commons.collections4.Bag;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ConcurrentLastFM {//implements LastFMService {
    public static final String GET_ALBUMS = "?method=user.gettopalbums&user=";
    public static final String GET_WEEKLY_CHART_TRACK = "?method=user.getWeeklyTrackChart&user=";
    public static final String GET_WEEKLY_CHART_ARTIST = "?method=user.getWeeklyArtistChart&user=";
    public static final String GET_WEEKLY_CHART_ALBUM = "?method=user.getWeeklyAlbumChart&user=";
    public static final String GET_ARTIST = "?method=user.gettopartists&user=";
    public static final String GET_TOP_TRACKS = "?method=user.gettoptracks&user=";
    static final String BASE = "http://ws.audioscrobbler.com/2.0/";
    static final String GET_LIBRARY = "?method=library.getartists&user=";
    static final String GET_USER = "?method=user.getinfo&user=";
    static final String ENDING = "&format=json";
    static final String RECENT_TRACKS = "?method=user.getrecenttracks";
    static final String LOVED_TRACKS = "?method=user.getlovedtracks";
    static final String GET_NOW_PLAYINH = RECENT_TRACKS + "&limit=1&user=";
    static final String GET_ALL = RECENT_TRACKS + "&limit=1000&user=";
    static final String GET_TRACKS = "?method=album.getinfo&username=";
    static final String GET_TRACK_INFO = "?method=track.getInfo&username=";
    static final String GET_CORRECTION = "?method=artist.getcorrection&artist=";
    static final String GET_ARTIST_ALBUMS = "?method=artist.gettopalbums&artist=";
    static final String GET_ARTIST_INFO = "?method=artist.getinfo&artist=";
    static final String GET_TRACK_TAGS = "?method=track.gettoptags";
    static final String GET_ALBUM_TAGS = "?method=album.gettoptags";
    static final String GET_ARTIST_TAGS = "?method=artist.gettoptags";
    static final String GET_USER_ARTIST_TAGS = "?method=artist.gettags";
    static final String GET_TAG_INFO = "?method=tag.getinfo&tag=";
    static final String GET_TOKEN = "?method=auth.gettoken";
    static final String GET_SESSION = "?method=auth.getSession&token=";
    static final String UPDATE_NP = "?method=track.updateNowPlaying&token=";
    private static final int SONG_AVERAGE_DURATION = 200;
    final String apiKey;
    final HttpClient client;
    private final @Nullable String secret;
    private final OAuthService oAuthService;

    public ConcurrentLastFM(String apikey, @Nullable String secret) {
        this.apiKey = "&api_key=" + apikey;
        this.secret = secret;
        this.client = ClientSingleton.getInstance();
        this.oAuthService = new OAuthService(Chuu.getDb(), this);

    }


    //@Override
    public NowPlayingArtist getNowPlayingInfo(LastFMData user) throws LastFmException {
        String url = BASE + GET_NOW_PLAYINH + user.getName() + apiKey + ENDING + "&extended=1";
        JSONObject obj = initGetRecentTracks(user, url, new CustomTimeFrame(TimeFrameEnum.ALL));
        boolean nowPlaying;

        JSONObject trackObj = obj.getJSONArray("track").getJSONObject(0);

        try {
            nowPlaying = trackObj.getJSONObject("@attr").getBoolean("nowplaying");
        } catch (JSONException e) {
            nowPlaying = false;
        }
        JSONObject artistObj = trackObj.getJSONObject("artist");
        String artistName = artistObj.getString("name");
        String mbid = artistObj.getString("mbid");

        String albumName = trackObj.getJSONObject("album").getString("#text");
        String songName = trackObj.getString("name");
        boolean loved = trackObj.getInt("loved") != 0;
        String imageUrl = obtainImage(trackObj);

        return new NowPlayingArtist(artistName, mbid, nowPlaying, albumName, songName, imageUrl, user.getName(), loved);


    }

    private String obtainImage(JSONObject jsonObject) {
        JSONArray images = jsonObject.getJSONArray("image");
        return images.getJSONObject(images.length() - 1).getString("#text").replace(".jpg", ".png");
    }

    private JSONObject initGetRecentTracks(LastFMData user, String url, CustomTimeFrame timeFrameEnum) throws LastFmException {
        JSONObject obj = doMethod(url, new ExceptionEntity(user.getName()), user);
        if (!obj.has("recenttracks")) {
            throw new LastFMNoPlaysException(user.getName(), timeFrameEnum);
        }
        obj = obj.getJSONObject("recenttracks");
        JSONObject attrObj = obj.getJSONObject("@attr");
        if (attrObj.getInt("total") == 0) {
            throw new LastFMNoPlaysException(user.getName(), timeFrameEnum);
        }
        JSONArray arr = obj.optJSONArray("track");
        if (arr == null) {
            JSONObject t = obj.getJSONObject("track");
            JSONArray objects = new JSONArray(List.of(t));
            obj.remove("track");
            obj.append("track", objects);
        }
        return obj;
    }

    private JSONObject doMethod(String url, ExceptionEntity causeOfNotFound, @Nullable LastFMData user) throws LastFmException {
        url = this.oAuthService.generateURL(url, user);
        HttpRequest method = createMethod(url);
        int counter = 0;
        while (true) {
            try {
                Chuu.incrementMetric();
                var cf = client.sendAsync(method, HttpResponse.BodyHandlers.ofString());
                var send = cf.get(30, TimeUnit.SECONDS);
                VirtualParallel.handleInterrupt();
                int responseCode = send.statusCode();
                parseHttpCode(responseCode);
                JSONObject jsonObject;
                if (responseCode == 404) {
                    throw new LastFmEntityNotFoundException(causeOfNotFound);
                }
                try {
                    jsonObject = new JSONObject(new JSONTokener(send.body()));
                } catch (JSONException exception) {
                    Chuu.getLogger().warn(exception.getMessage(), exception);
                    Chuu.getLogger().warn("JSON Exception doing url: {}, code: {}, ", method.uri(), responseCode);
                    throw new ChuuServiceException(exception);
                }
                if (jsonObject.has("error")) {
                    parseResponse(jsonObject, causeOfNotFound, user);
                }
                if (Math.floor((float) responseCode / 100) == 4) {
                    Chuu.getLogger().warn("Error {} with url {}", responseCode, method.uri().toString());
                    throw new UnknownLastFmException(jsonObject.toString(), responseCode, user);
                }

                return jsonObject;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                VirtualParallel.handleInterrupt();
                throw new ChuuServiceException(e);
            } catch (LastFMServiceException e) {
                VirtualParallel.handleInterrupt();
                Chuu.getLogger().warn("LAST.FM Internal Error | URI {} | Message {} ", method.uri().toString(), e.getMessage(), e);
            } catch (ExecutionException | TimeoutException e) {
                VirtualParallel.handleInterrupt();
                Chuu.getLogger().warn("LAST.FM Future Error | URI {} | Message {} ", method.uri().toString(), e.getMessage(), e);
            }
            if (++counter == 2) {
                throw new LastFMConnectionException("500");
            }

        }

    }

    private void parseHttpCode(int code) throws LastFmException {
        if (code / 100 == 2)
            return;
        if (code == 500)
            throw new LastFMServiceException("500");

    }

    private void parseResponse(JSONObject jsonObject, ExceptionEntity exceptionEntity, LastFMData user) throws LastFmEntityNotFoundException, UnknownLastFmException {
        int code = jsonObject.getInt("error");
        if (code == 6) {
            throw new LastFmEntityNotFoundException(exceptionEntity);
        } else throw new UnknownLastFmException(jsonObject.toString(), code, user);
    }

    private HttpRequest createMethod(String url) {
        return HttpRequest.newBuilder()
                .GET()
                .timeout(Duration.ofSeconds(5))
                .uri(URI.create(url))
                .setHeader("User-Agent", "discordBot/ishwi6@gmail.com") // add request header
                .build();

    }

    private HttpRequest createPost(PostEntity postEntity) {
        String form = getSignature(postEntity);
        return HttpRequest.newBuilder()
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .uri(URI.create(BASE + "?format=json"))
                .setHeader("User-Agent", "discordBot/ishwi6@gmail.com") // add request header
                .build();
    }

    //@Override
    public List<UserInfo> getUserInfo(List<String> lastFmNames, LastFMData requester) throws LastFmException {
        List<UserInfo> returnList = new ArrayList<>();

        for (String lastFmName : lastFmNames) {
            String url = BASE + GET_USER + lastFmName + apiKey + ENDING;
            JSONObject obj = doMethod(url, new ExceptionEntity(lastFmName), requester);
            obj = obj.getJSONObject("user");
            String image2 = obtainImage(obj);
            int unixTime = obj.getJSONObject("registered").getInt("#text");
            int playCount = obj.getInt("playcount");
            returnList.add(new UserInfo(playCount, image2, lastFmName, unixTime));

        }

        return returnList;

    }

    public List<ArtistInfo> getTopArtists(LastFMData user, CustomTimeFrame timeframe, int requestedSize) throws LastFmException {
        List<ArtistInfo> returnList = new ArrayList<>();
        TimeFrameEnum timeFrameEnum = timeframe.getTimeFrameEnum();
        if (!timeframe.isNormal()) {
            return getCustomT(TopEntity.ARTIST, user, capsule -> new ArtistInfo(null, capsule.getArtistName(), capsule.getMbid()), requestedSize, timeframe);
        }
        if (timeFrameEnum == TimeFrameEnum.DAY) {
            return getDailyT(TopEntity.ARTIST, user, capsule -> new ArtistInfo(null, capsule.getArtistName(), capsule.getMbid()), requestedSize);
        }
        int size = 0;
        String url = BASE + GET_ARTIST + user.getName() + apiKey + ENDING + "&period=" + timeFrameEnum.toApiFormat();
        int page = 1;
        if (requestedSize >= 1000)
            url += "&limit=1000";
        else if (requestedSize > 700)
            url += "&limit=500";
        else if (requestedSize > 150)
            url += "&limit=200";

        int limit = requestedSize;
        while (size < requestedSize && size < limit) {

            String urlPage = url + "&page=" + page;

            ++page;

            // Execute the method.
            JSONObject obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            obj = obj.getJSONObject("topartists");
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
            limit = obj.getJSONObject("@attr").getInt("total");
            if (limit == 0) {
                throw new LastFMNoPlaysException(user.getName(), new CustomTimeFrame(timeFrameEnum));
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

    private <X> List<X> getDailyT(TopEntity topEntity, LastFMData username, Function<UrlCapsule, X> mapper, int requestedSize) throws LastFmException {
        BlockingQueue<UrlCapsule> objects = new LinkedBlockingDeque<>();
        int sqrt = (int) Math.floor(Math.sqrt(requestedSize));
        getDailyChart(username, objects, ChartUtil.getParser(new CustomTimeFrame(TimeFrameEnum.DAY), topEntity, ChartParameters.toListParams(), this, username), sqrt, sqrt);
        ArrayList<UrlCapsule> urlCapsules = new ArrayList<>();
        int i = objects.drainTo(urlCapsules);
        return urlCapsules.stream().sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed()).map(mapper).limit(requestedSize).toList();
    }

    private <X> List<X> getCustomT(TopEntity topEntity, LastFMData username, Function<UrlCapsule, X> mapper, int requestedSize, CustomTimeFrame timeFrame) throws LastFmException {
        BlockingQueue<UrlCapsule> objects = new LinkedBlockingDeque<>();
        Pair<Long, Long> fromTo = ChartUtil.getFromTo(timeFrame);
        BiFunction<JSONObject, Integer, UrlCapsule> parser = ChartUtil.getParser(timeFrame, topEntity, ChartParameters.toListParams(), this, username);
        getRangeChartChart(username, fromTo.getLeft(), fromTo.getRight(), requestedSize, 1, topEntity, parser, objects, timeFrame);
        ArrayList<UrlCapsule> urlCapsules = new ArrayList<>();
        int i = objects.drainTo(urlCapsules);
        return urlCapsules.stream().sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed()).map(mapper).limit(requestedSize).toList();
    }

    public List<AlbumInfo> getTopAlbums(LastFMData userName, CustomTimeFrame timeFrame, int requestedSize) throws LastFmException {
        List<AlbumInfo> returnList = new ArrayList<>();
        if (timeFrame.getType() != CustomTimeFrame.Type.NORMAL) {
            return getCustomT(TopEntity.ALBUM, userName, capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()), requestedSize, timeFrame);
        }
        TimeFrameEnum period = timeFrame.getTimeFrameEnum();
        if (period == TimeFrameEnum.DAY) {
            return getDailyT(TopEntity.ALBUM, userName, capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()), requestedSize);
        }
        int size = 0;
        String url = BASE + GET_ALBUMS + userName.getName() + apiKey + ENDING + "&period=" + period.toApiFormat();
        int page = 1;
        if (requestedSize >= 1000)
            url += "&limit=1000";
        else
            url += "&limit=" + requestedSize;

        int limit = requestedSize;
        while (size < requestedSize && size < limit) {

            String urlPage = url + "&page=" + page;

            ++page;

            JSONObject obj = doMethod(urlPage, new ExceptionEntity(userName.getName()), userName);
            obj = obj.getJSONObject("topalbums");
            limit = obj.getJSONObject("@attr").getInt("total");
            if (limit == 0) {
                throw new LastFMNoPlaysException(userName.getName(), new CustomTimeFrame(period));
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

    private int getDailyChart(LastFMData user, Queue<UrlCapsule> queue, BiFunction<JSONObject, Integer, UrlCapsule> parser, int x, int y) throws LastFmException {
        String url = BASE + GET_ALL + user.getName() + apiKey + ENDING;
        long time = OffsetDateTime.now().atZoneSameInstant(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS).toEpochSecond();

        url += "&from=" + (time);

        int page = 0;
        int total = 1;
        int count = 0;
        List<UrlCapsule> items = new ArrayList<>();
        while (count < total) {
            String urlPage = url + "&page=" + ++page;
            JSONObject obj = initGetRecentTracks(user, urlPage, new CustomTimeFrame(TimeFrameEnum.DAY));

            JSONObject attrObj = obj.getJSONObject("@attr");
            total = attrObj.getInt("total");
            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {

                JSONObject trackObj = arr.getJSONObject(i);
                if (trackObj.has("@attr"))
                    continue;
                UrlCapsule apply = parser.apply(trackObj, 0);
                items.add(apply);
                count++;
            }
        }
        Map<UrlCapsule, Long> itemCount = items.stream().collect(Collectors.groupingBy(z -> z, Collectors.counting()));
        for (Map.Entry<UrlCapsule, Long> urlCapsuleLongEntry : itemCount.entrySet()) {
            UrlCapsule key = urlCapsuleLongEntry.getKey();
            if (key instanceof TrackDurationChart trackDuratio) {
                trackDuratio.setSeconds(Math.toIntExact(trackDuratio.getSeconds() * urlCapsuleLongEntry.getValue()));
            }
            key.setPlays(Math.toIntExact(urlCapsuleLongEntry.getValue()));
        }
        AtomicInteger integer = new AtomicInteger(0);
        List<UrlCapsule> finalList = new ArrayList<>(itemCount.keySet()).stream().sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                .takeWhile(t -> {
                    int i = integer.getAndIncrement();
                    t.setPos(i);
                    return (i < x * y);
                }).toList();
        queue.addAll(finalList);
        return itemCount.entrySet().size();


    }

    public int getChart(LastFMData user, CustomTimeFrame customTimeFrame, int x, int y, TopEntity entity, BiFunction<JSONObject, Integer, UrlCapsule> parser, Queue<UrlCapsule> queue) throws
            LastFmException {
        if (!customTimeFrame.isNormal()) {
            return doCustomChart(user, customTimeFrame, x, y, entity, parser, queue);
        }
        TimeFrameEnum timeFrameEnum = customTimeFrame.getTimeFrameEnum();
        if (timeFrameEnum == TimeFrameEnum.DAY) {
            return getDailyChart(user, queue, parser, x, y);
        }
        int requestedSize = x * y;
        String apiMethod = entity.getApiMethod();
        String leadingObject = entity.getLeadingObject();
        String arrayObject = entity.getArrayObject();

        String url = BASE + apiMethod + user.getName() + apiKey + ENDING + "&period=" + timeFrameEnum.toApiFormat();
        int size = 0;
        int page = 1;
        if (requestedSize >= 1000) {
            if (entity == TopEntity.ALBUM && y == 100) {
                url += "&limit=500";
            } else {
                url += "&limit=1000";
            }
        } else
            url += "&limit=" + requestedSize;


        int limit = requestedSize;
        while (size < requestedSize && size < limit) {

            String urlPage = url + "&page=" + page;
            ++page;

            // Execute the method.
            JSONObject obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            obj = obj.getJSONObject(leadingObject);
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
            limit = obj.getJSONObject("@attr").getInt("total");
            if (limit == 0) {
                throw new LastFMNoPlaysException(user.getName(), customTimeFrame);
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

    private int doCustomChart(LastFMData user, CustomTimeFrame customTimeFrame, int x, int y, TopEntity entity, BiFunction<JSONObject, Integer, UrlCapsule> parser, Queue<UrlCapsule> queue) throws LastFmException {
        Pair<Long, Long> fromTo = ChartUtil.getFromTo(customTimeFrame);
        return getRangeChartChart(user, fromTo.getLeft(), fromTo.getRight(), x, y, entity, parser, queue, customTimeFrame);
    }


    public SecondsTimeFrameCount getMinutesWastedOnMusic(LastFMData user, TimeFrameEnum timeFrameEnum) throws LastFmException {
        String period = timeFrameEnum.toApiFormat();
        String url = BASE + GET_TOP_TRACKS + user.getName() + apiKey + ENDING + "&period=" + period + "&limit=1000";
        int page = 1;
        int total = 1;
        TimeFrameEnum timeFrame = TimeFrameEnum.fromCompletePeriod(period);
        SecondsTimeFrameCount returned = new SecondsTimeFrameCount(timeFrame);
        if (timeFrame.equals(TimeFrameEnum.DAY)) {
            List<Track> listTopTrack = getListTopTrack(user, TimeFrameEnum.DAY);
            int secondCounter = 0;
            for (Track track : listTopTrack) {
                secondCounter += track.getDuration() == 0 ? SONG_AVERAGE_DURATION * track.getPlays() : track.getDuration() * track.getPlays();
            }
            returned.setCount(listTopTrack.size());
            returned.setSeconds(secondCounter);
            return returned;
        }
        int count = 0;
        int seconds = 0;
        while (page <= total) {

            // Execute the method.
            JSONObject obj = doMethod(url + "&page=" + (page), new ExceptionEntity(user.getName()), user);
            obj = obj.getJSONObject("toptracks");
            if (page == 1) {
                total = obj.getJSONObject("@attr").getInt("totalPages");
                count = obj.getJSONObject("@attr").getInt("total");
            }
            ++page;

            JSONArray arr = obj.getJSONArray("track");
            seconds += StreamSupport.stream(arr.spliterator(), false).mapToInt(x -> {
                JSONObject jsonObj = ((JSONObject) x);
                int duration = parseDuration(jsonObj, true);
                int frequency = jsonObj.getInt("playcount");
                return duration * frequency;
            }).sum();


        }
        returned.setCount(count);
        returned.setSeconds(seconds);
        return returned;

    }


    public Map<Track, Integer> getTrackDurations(LastFMData user, TimeFrameEnum timeFrameEnum) throws LastFmException {
        Map<Track, Integer> trackList = new HashMap<>();
        String url = BASE + GET_TOP_TRACKS + user.getName() + apiKey + ENDING + "&period=" + timeFrameEnum
                .toApiFormat() + "&limit=1000";
        int page = 1;
        int total = 1;
        boolean doWhole = timeFrameEnum.equals(TimeFrameEnum.WEEK);
        while (page <= total && (doWhole || page <= 2)) {

            // Execute the method.
            JSONObject obj = doMethod(url + "&page=" + (page), new ExceptionEntity(user.getName()), user);

            obj = obj.getJSONObject("toptracks");
            if (page == 1) {
                total = obj.getJSONObject("@attr").getInt("totalPages");
            }
            ++page;

            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jsonObj = (arr.getJSONObject(i));
                String name = jsonObj.getString("name");
                int duration = parseDuration(jsonObj, true);
                int frequency = jsonObj.getInt("playcount");
                String artist_name = jsonObj.getJSONObject("artist").getString("name");

                trackList.put(new Track(artist_name, name, 0, false, 0), duration);
            }

        }
        return trackList;

    }

    //TODO can do anythign with from and to timestamp
    public SecondsTimeFrameCount getMinutesWastedOnMusicDaily(LastFMData user, Map<Track, Integer> trackList, int timestampQuery) throws LastFmException {
        String url = BASE + GET_ALL + user.getName() + apiKey + ENDING + "&extended=1" + "&from=" + (timestampQuery);
        SecondsTimeFrameCount returned = new SecondsTimeFrameCount(TimeFrameEnum.ALL);
        Map<String, String> validatedArtist = new HashMap<>();
        int count = 0;
        int seconds = 0;
        int page = 0;
        int total = 1;
        while (count < total) {
            String urlPage = url + "&page=" + ++page;
            JSONObject obj = initGetRecentTracks(user, urlPage, new CustomTimeFrame(TimeFrameEnum.WEEK));
            JSONObject attrObj = obj.getJSONObject("@attr");
            total = attrObj.getInt("total");
            if (!obj.has("track") && obj.optJSONArray("track") == null) {
                throw new LastFMNoPlaysException(user.getName(), new CustomTimeFrame(TimeFrameEnum.WEEK));
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
                } else {
                    String s = validatedArtist.get(track.getArtist());
                    if (s == null) {
                        try {
                            s = Chuu.getDb().getReverseCorrection(track.getArtist());
                            validatedArtist.put(track.getArtist(), s);
                        } catch (Exception ignored) {
                        }
                    }
                    track = new Track(s, track.getName(), 0, false, 0);
                    if ((duration = trackList.get(track)) != null) {
                        seconds += duration;
                    } else
                        seconds += 0;
                }
                count++;
            }

        }
        returned.setCount(count);
        returned.setSeconds(seconds);
        return returned;
    }

    public int getInfoPeriod(LastFMData username, long timestampQuery) throws LastFmException {
        String url = BASE + GET_NOW_PLAYINH + username.getName() + apiKey + ENDING + "&extended=1" + "&from=" + (timestampQuery);
        JSONObject obj = doMethod(url, new ExceptionEntity(username.getName()), username);
        obj = obj.getJSONObject("recenttracks");
        JSONObject attrObj = obj.getJSONObject("@attr");
        return attrObj.getInt("total");
    }

    public StreakEntity getCombo(LastFMData lastFMData) throws LastFmException {
        String url = BASE + RECENT_TRACKS + "&user=" + lastFMData.getName() + apiKey + ENDING + "&extended=1";
        int page = 0;
        String currentArtist = null;
        String currentAlbum = null;
        String currentSong = null;
        String image = null;
        int aCounter = 0;
        int albCounter = 0;
        int tCounter = 0;
        boolean inited = false;
        Instant streakStart = null;
        boolean stopAlbCounter = false;
        boolean stopArtistCounter = false;
        boolean stopTCounter = false;
        boolean cont = true;
        boolean restarting = false;
        boolean restarted = false;
        long previousUts = Instant.now().getEpochSecond();
        int totalPages = 1;
        while (cont) {
            String comboUrl;
            if (page == 0) {
                comboUrl = url + "&limit=50";
            } else {
                if (restarting)
                    page--;
                comboUrl = url + "&limit=1000";
            }
            String urlPage = comboUrl + "&page=" + ++page;
            if (page == 11 || page > totalPages) {
                break;
            }
            JSONObject obj = initGetRecentTracks(lastFMData, urlPage, new CustomTimeFrame(TimeFrameEnum.ALL));
            if (page == 1) {
                totalPages = obj.getJSONObject("@attr").getInt("totalPages");
            }

            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                if (restarting) {
                    i = 50;
                    restarting = false;
                    if (arr.getJSONObject(0).has("@attr")) {
                        i++;
                    }

                }
                JSONObject trackObj = arr.getJSONObject(i);
                if (aCounter != 0 && trackObj.has("@attr")) {
                    continue;
                }

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
                        previousUts = trackObj.getJSONObject("date").getLong("uts");
                        aCounter++;
                    } else {
                        stopArtistCounter = true;
                    }
                    if (!stopAlbCounter && currentAlbum.equals(albumString)) {
                        if (stopArtistCounter) {
                            previousUts = trackObj.getJSONObject("date").getLong("uts");
                        }
                        albCounter++;
                    } else {
                        stopAlbCounter = true;
                        if (stopArtistCounter) {
                            cont = false;
                            streakStart = Instant.ofEpochSecond(previousUts);
                            break;
                        }
                    }
                    if (!stopTCounter && currentSong.equals(trackName)) {
                        tCounter++;
                        if ((image == null || image.isBlank()))
                            image = obtainImage(trackObj);
                    } else
                        stopTCounter = true;
                } else {
                    if (!stopAlbCounter) {
                        albCounter++;
                        currentAlbum = albumString;
                    }
                    currentArtist = artistName;
                    currentSong = trackName;
                    aCounter++;
                    if (!trackObj.has("@attr"))
                        previousUts = trackObj.getJSONObject("date").getLong("uts");
                    tCounter++;
                    inited = true;
                }
            }
            if (page == 1 && !restarted) {
                restarting = true;
                restarted = true;
            }
        }
        if (streakStart == null)
            streakStart = Instant.EPOCH;
        return new

                StreakEntity(currentArtist, aCounter, currentAlbum, albCounter, currentSong, tCounter, streakStart, image);

    }

    public Bag<Genre> getTagCombo(LastFMData lastFMData, TriFunction<String, String, String, Set<Genre>> tagFactory) throws LastFmException {
        String url = "%s%s&user=%s%s%s&extended=1&limit=1000".formatted(BASE, RECENT_TRACKS, lastFMData.getName(), apiKey, ENDING);

        int page = 0;
        boolean cont = true;
        int totalPages = 1;
        boolean init = false;
        Bag<Genre> bag = new UniqueBag<>(Genre.class);
        Set<Genre> currentSet = new HashSet<>();
        int nullCounts = 0;
        while (cont) {
            String urlPage = url + "&page=" + ++page;
            if (page == 4 || page > totalPages) {
                break;
            }
            JSONObject obj = initGetRecentTracks(lastFMData, urlPage, new CustomTimeFrame(TimeFrameEnum.ALL));
            if (page == 1) {
                totalPages = obj.getJSONObject("@attr").getInt("totalPages");
            }
            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                if (nullCounts >= 2) {
                    return bag;
                }
                JSONObject trackObj = arr.getJSONObject(i);
                String trackName = trackObj.getString("name");
                JSONObject artistObj = trackObj.getJSONObject("artist");
                String artistName = artistObj.getString("name");
                String albumString = null;
                JSONObject album = trackObj.getJSONObject("album");
                if (album.has("#text")) {
                    albumString = album.getString("#text");

                }
                Set<Genre> tags = tagFactory.apply(artistName, trackName, albumString);
                if (tags == null) {
                    nullCounts++;
                    continue;
                } else {
                    nullCounts = 0;
                }
                if (init) {
                    currentSet.retainAll(tags);
                    if (currentSet.isEmpty()) {
                        cont = false;
                        break;
                    }
                    bag.addAll(currentSet);
                } else {
                    init = true;
                    bag.addAll(tags);
                    currentSet.addAll(tags);
                }
            }
        }
        return bag;
    }


    public List<NowPlayingArtist> getRecent(LastFMData user, int limit) throws LastFmException {
        String url = BASE + RECENT_TRACKS + "&user=" + user.getName() + "&limit=" + limit + apiKey + ENDING + "&extended=1";
        JSONObject obj = initGetRecentTracks(user, url, new CustomTimeFrame(TimeFrameEnum.ALL));
        List<NowPlayingArtist> npList = new ArrayList<>();
        JSONArray arr = obj.getJSONArray("track");

        for (int i = 0; i < arr.length() && npList.size() < limit; i++) {
            JSONObject trackObj = arr.getJSONObject(i);
            JSONObject artistObj = trackObj.getJSONObject("artist");

            boolean np = trackObj.has("@attr");

            String artistName = artistObj.getString("name");
            String albumName = trackObj.getJSONObject("album").getString("#text");
            String songName = trackObj.getString("name");
            String image = obtainImage(trackObj);

            npList.add(new NowPlayingArtist(artistName, "", np, albumName, songName, image, user.getName(), false));
        }
        return npList;
    }


    @NotNull
    public List<ScrobbledArtist> getAllArtists(LastFMData user, CustomTimeFrame customTimeFrame) throws
            LastFmException {
        if (customTimeFrame.getType() != CustomTimeFrame.Type.NORMAL) {
            return getCustomT(TopEntity.ARTIST, user, capsule -> {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(capsule.getArtistName(), capsule.getPlays(), null);
                scrobbledArtist.setArtistMbid(capsule.getMbid());
                return scrobbledArtist;
            }, 1000, customTimeFrame);
        }
        TimeFrameEnum period = customTimeFrame.getTimeFrameEnum();
        if (period == TimeFrameEnum.DAY) {
            return getDailyT(TopEntity.ARTIST, user, capsule -> {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(capsule.getArtistName(), capsule.getPlays(), null);
                scrobbledArtist.setArtistMbid(capsule.getMbid());
                return scrobbledArtist;
            }, 3000);
        }
        String url = BASE + GET_ARTIST + user.getName() + apiKey + ENDING + "&period=" + period.toApiFormat();

        int page = 1;
        int pages = 1;
        url += "&limit=1000";

        List<ScrobbledArtist> scrobbledArtistData = new ArrayList<>();
        while (page <= pages) {

            String urlPage = url + "&page=" + page;
            // Execute the method.
            JSONObject obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            String topObject = "topartists";
            obj = obj.getJSONObject(topObject);
            if (page++ == 1) {
                pages = obj.getJSONObject("@attr").getInt("totalPages");
                if (obj.getJSONObject("@attr").getInt("total") == 0) {
                    throw new LastFMNoPlaysException(user.getName());
                }
            }
            JSONArray arr = obj.getJSONArray("artist");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject artistObj = arr.getJSONObject(i);
                String artistName = artistObj.getString("name");
                String mbid = artistObj.getString("mbid");
                int count = artistObj.getInt("playcount");
                ScrobbledArtist e = new ScrobbledArtist(artistName, count, null);
                e.setArtistMbid(mbid);
                scrobbledArtistData.add(e);
            }
        }
        return scrobbledArtistData;
    }

    @NotNull
    public List<ScrobbledTrack> getAllTracks(LastFMData user, CustomTimeFrame timeFrame) throws LastFmException {
        if (timeFrame.getType() != CustomTimeFrame.Type.NORMAL) {
            return getCustomT(TopEntity.TRACK, user, capsule -> new ScrobbledTrack(capsule.getArtistName(), capsule.getAlbumName(), capsule.getPlays(), false, SONG_AVERAGE_DURATION, capsule.getUrl(), null, capsule.getMbid()), 1000, timeFrame);
        }
        TimeFrameEnum period = timeFrame.getTimeFrameEnum();
        if (period == TimeFrameEnum.DAY) {
            return getDailyT(TopEntity.TRACK, user, capsule -> new ScrobbledTrack(capsule.getArtistName(), capsule.getAlbumName(), capsule.getPlays(), false, SONG_AVERAGE_DURATION, capsule.getUrl(), null, capsule.getMbid()), 3000);
        }

        String url = BASE + GET_TOP_TRACKS + user.getName() + apiKey + ENDING + "&period=" + period.toApiFormat();

        int page = 1;
        int pages = 1;
        url += "&limit=1000";

        List<ScrobbledTrack> list = new ArrayList<>();
        while (page <= pages) {

            String urlPage = url + "&page=" + page;
            JSONObject obj;
            try {
                obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            } catch (LastFMConnectionException e) {
                if (page >= pages) {
                    return list;
                }
                obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            }
            String topObject = "toptracks";
            obj = obj.getJSONObject(topObject);
            if (page++ == 1) {
                pages = obj.getJSONObject("@attr").getInt("totalPages");
                if (obj.getJSONObject("@attr").getInt("total") == 0) {
                    throw new LastFMNoPlaysException(user.getName());
                }
            }
            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jsonObj = (arr.getJSONObject(i));
                String name = jsonObj.getString("name");
                String mbid = jsonObj.getString("mbid");
                String imageUrl = obtainImage(jsonObj);

                int duration = parseDuration(jsonObj, true);
                int frequency = jsonObj.getInt("playcount");
                JSONObject artist = jsonObj.getJSONObject("artist");
                String artistName = artist.getString("name");
                String artistMbid = artist.getString("mbid");
                ScrobbledTrack e = new ScrobbledTrack(artistName, name, frequency, false, duration, imageUrl, artistMbid, mbid);
                list.add(e);
            }
        }
        return list;
    }

    public JSONArray getFMArr(JSONObject root, String key) {
        JSONArray arr = root.optJSONArray(key);
        if (arr == null) {
            JSONObject t = root.getJSONObject(key);
            arr = new JSONArray(List.of(t));
        }
        return arr;

    }

    @NotNull
    public List<ScrobbledAlbum> getAllAlbums(LastFMData user, CustomTimeFrame timeFrame) throws LastFmException {
        if (timeFrame.getType() != CustomTimeFrame.Type.NORMAL) {
            return getCustomT(TopEntity.ALBUM, user, capsule -> {
                ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(capsule.getAlbumName(), capsule.getArtistName(), capsule.getUrl(), capsule.getMbid());
                scrobbledAlbum.setCount(capsule.getPlays());
                return scrobbledAlbum;
            }, 1000, timeFrame);
        }
        TimeFrameEnum period = timeFrame.getTimeFrameEnum();
        if (period == TimeFrameEnum.DAY) {
            return getDailyT(TopEntity.ALBUM, user, capsule -> {
                ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(capsule.getAlbumName(), capsule.getArtistName(), capsule.getUrl(), capsule.getMbid());
                scrobbledAlbum.setCount(capsule.getPlays());
                return scrobbledAlbum;
            }, 3000);
        }

        String url = BASE + GET_ALBUMS + user.getName() + apiKey + ENDING + "&period=" + period.toApiFormat();

        int page = 1;
        int pages = 1;
        url += "&limit=1000";

        List<ScrobbledAlbum> list = new ArrayList<>();
        while (page <= pages) {

            String urlPage = url + "&page=" + page;
            JSONObject obj;
            try {
                obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            } catch (LastFMConnectionException e) {
                if (page >= pages) {
                    return list;
                }
                obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            }
            String topObject = "topalbums";
            obj = obj.getJSONObject(topObject);
            if (page++ == 1) {
                pages = obj.getJSONObject("@attr").getInt("totalPages");
                if (obj.getJSONObject("@attr").getInt("total") == 0) {
                    throw new LastFMNoPlaysException(user.getName());
                }
            }
            JSONArray arr = obj.getJSONArray("album");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject albumObj = arr.getJSONObject(i);
                JSONObject artistObj = albumObj.getJSONObject("artist");
                String albumName = albumObj.getString("name");
                String imageUrl = obtainImage(albumObj);

                int playCount = albumObj.getInt("playcount");
                String albumMbid = albumObj.getString("mbid");
                String artistMbid = artistObj.getString("mbid");
                String artistName = artistObj.getString("name");
                ScrobbledAlbum e = new ScrobbledAlbum(albumName, artistName, imageUrl, albumMbid);
                e.setArtistMbid(artistMbid);
                e.setCount(playCount);
                list.add(e);
            }
        }
        return list;
    }

    public String getCorrection(String artistToCorrect) throws LastFmException {
        String url;
        url = BASE + GET_CORRECTION + URLEncoder.encode(artistToCorrect, StandardCharsets.UTF_8) + apiKey + ENDING;
        JSONObject obj = doMethod(url, new ArtistException(artistToCorrect), null);
        if (obj.has("corrections") && obj.get("corrections") instanceof String)
            throw new LastFmEntityNotFoundException(new ArtistException(artistToCorrect));

        obj = obj.getJSONObject("corrections");
        JSONObject artistObj = obj.getJSONObject("correction").getJSONObject("artist");
        return artistObj.getString("name");
    }

    public AlbumUserPlays getPlaysAlbumArtist(LastFMData user, String artist, String album) throws
            LastFmException {

        JSONObject obj = initAlbumJSON(user, artist, album);
        artist = obj.getString("artist");
        album = obj.getString("name");
        String imageUrl = obtainImage(obj);

        AlbumUserPlays ai = new AlbumUserPlays(album, imageUrl);
        if (obj.has("userplaycount")) {
            ai.setPlays(obj.getInt("userplaycount"));
        } else {
            ai.setPlays(0);
        }
        ai.setArtist(artist);
        return ai;
    }

    private JSONObject initAlbumJSON(LastFMData user, String artist, String album) throws LastFmException {
        String url;
        url = BASE + GET_TRACKS + user.getName() + "&artist=" + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&album=" + URLEncoder.encode(album, StandardCharsets.UTF_8) +
              apiKey + ENDING + "&autocorrect=1";

        JSONObject obj = doMethod(url, new AlbumException(artist, album), user);
        obj = obj.getJSONObject("album");
        return obj;
    }

    public FullAlbumEntity getTracksAlbum(LastFMData user, String artist, String album) throws
            LastFmException {
        JSONObject obj = initAlbumJSON(user, artist, album);

        String imageUrl = obtainImage(obj);
        String correctedArtist = obj.getString("artist");
        String correctedAlbum = obj.getString("name");

        if (!obj.has("userplaycount")) {
            throw new LastFmEntityNotFoundException(new ExceptionEntity(user.getName()));
        }

        int playCount = obj.optInt("userplaycount", 0);
        FullAlbumEntity fae = new FullAlbumEntity(correctedArtist, correctedAlbum, playCount, imageUrl, user.getName());
        JSONObject tracks = obj.optJSONObject("tracks");
        if (tracks != null) {
            JSONArray arr = getFMArr(tracks, "track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject trackObj = arr.getJSONObject(i);
                String trackName = trackObj.getString("name");
                Track trackInfo = this.getTrackInfo(user, correctedArtist, trackName);
                trackInfo.setPosition(trackObj.getJSONObject("@attr").getInt("rank"));
                fae.addTrack(trackInfo);
            }
        }
        return fae;
    }

    public Track getTrackInfo(LastFMData user, String artist, String trackName) throws LastFmException {
        String url = BASE + GET_TRACK_INFO + user.getName() + "&artist=" + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder
                             .encode(trackName, StandardCharsets.UTF_8) +
                     apiKey + ENDING + "&autocorrect=1";
        ExceptionEntity exceptionEntity = new TrackException(artist, trackName);
        JSONObject obj = doMethod(url, exceptionEntity, user);
        obj = obj.getJSONObject("track");
        int userplaycount = 0;
        if (obj.has("userplaycount")) {
            userplaycount = obj.getInt("userplaycount");
        }
        String reTrackName = obj.getString("name");
        String mbid = obj.optString("mbid");

        boolean userloved = false;
        if (obj.has("userloved")) {
            userloved = obj.getInt("userloved") != 0;
        }
        int duration = parseDuration(obj, false);
        String reArtist = obj.getJSONObject("artist").getString("name");

        Track track = new Track(reArtist, reTrackName, userplaycount, userloved, duration);
        track.setMbid(mbid);

        JSONObject images;
        if ((images = obj).has("album") && (images = images.getJSONObject("album")).has("image")) {
            track.setImageUrl(obtainImage(images));
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


        JSONObject obj = doMethod(url, new ArtistException(artist), null);

        obj = obj.getJSONObject("topalbums");
        artistCorrected = obj.getJSONObject("@attr").getString("artist");

        JSONArray arr = obj.getJSONArray("album");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject tempObj = arr.getJSONObject(i);
            String imageUrl = obtainImage(tempObj);
            albumList.add(new AlbumUserPlays(tempObj.getString("name"), imageUrl));
        }

        return new ArtistAlbums(artistCorrected, albumList);
    }

    public int getTotalAlbumCount(LastFMData user) throws LastFmException {
        String url = BASE + GET_ALBUMS + user.getName() + apiKey + ENDING + "&period=overall&limit=1";
        return doMethod(url, new ExceptionEntity(user.getName()), user).getJSONObject("topalbums").getJSONObject("@attr")
                .getInt("total");

    }

    public List<Track> getTopArtistTracks(LastFMData user, String artist, TimeFrameEnum timeFrame, String
            correction) throws LastFmException {
        final int SIZE_LIMIT = 10;

        List<Track> trackList = new ArrayList<>();

        int artistPlays = this.getArtistPlays(artist, user);
        if (artistPlays == 0) {
            return trackList;
        }

        String url;
        int page = 1;
        boolean cont = true;
        boolean dontdoAll = true;

        int limit = 2;

        url = BASE + GET_TOP_TRACKS + user.getName() +
              apiKey + "&limit=" + 1000 + ENDING + "&period=" + timeFrame.toApiFormat();

        if (List.of(TimeFrameEnum.DAY, TimeFrameEnum.WEEK, TimeFrameEnum.MONTH).contains(timeFrame)) {
            dontdoAll = false;
        }

        while (trackList.size() < SIZE_LIMIT && page < limit && cont) {

            String urlPage = url + "&page=" + page;
            ++page;
            // Execute the method.
            JSONObject obj = doMethod(urlPage, new ExceptionEntity(user.getName()), user);
            obj = obj.getJSONObject("toptracks");
//			if (page== 2)
//				requestedSize = obj.getJSONObject("@attr").getInt("total");
            limit = Math.min(5, obj.getJSONObject("@attr").getInt("totalPages"));
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

                if (artistName.equalsIgnoreCase(artist) || correction.equalsIgnoreCase(artistName)) {
                    String trackName = trackObj.getString("name");
                    int duration = parseDuration(trackObj, false);
                    Track track = new Track(artist, trackName, userplaycount, false, duration);
                    trackList.add(track);
                    if (trackList.size() == SIZE_LIMIT)
                        break;
                }

            }

        }
        return trackList;
    }

    private int getArtistPlays(String artist, LastFMData user) throws LastFmException {
        String url;
        url = BASE + GET_ARTIST_INFO + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&username=" + user.getName() + apiKey + "&limit=" + 1000 + ENDING;

        // Execute the method.

        JSONObject jsonObject = doMethod(url, new ArtistException(artist), user).getJSONObject("artist");

        if (jsonObject.getJSONObject("stats").has("userplaycount")) {
            return jsonObject.getJSONObject("stats").getInt("userplaycount");
        } else
            return 0;

    }

    public ArtistSummary getArtistSummary(String artist, LastFMData user) throws LastFmException {
        String url;
        url = BASE + GET_ARTIST_INFO + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&username=" + user.getName() + apiKey + "&limit=" + 1000 + ENDING;
        // Execute the method.
        JSONObject jsonObject = doMethod(url, new ArtistException(artist), user);
        JSONObject globalJson = jsonObject.getJSONObject("artist");
        JSONObject statObject = globalJson.getJSONObject("stats");
        int userPlayCount = statObject.optInt("userplaycount", 0);
        int listeners = statObject.getInt("listeners");
        int playcount = statObject.getInt("playcount");
        String mbid = null;
        if (globalJson.has("mbid")) {
            mbid = globalJson.getString("mbid");
        }
        String artistName = globalJson.getString("name");
        JSONObject similar = globalJson.optJSONObject("similar");
        List<String> similars;
        if (similar != null) {
            JSONArray artistArray = getFMArr(similar, "artist");
            similars = StreamSupport.stream(artistArray.spliterator(), false).map(JSONObject.class::cast)
                    .map(x -> x.getString("name")).toList();
        } else {
            similars = Collections.emptyList();
        }
        List<String> tags = parseTags(globalJson);


        JSONObject bio = globalJson.optJSONObject("bio");
        String summary;
        if (bio != null) {
            String field = bio.getString("summary");
            int i = field.indexOf("<a");
            summary = field.substring(0, i);
        } else {
            summary = "";
        }

        return new ArtistSummary(userPlayCount, listeners, playcount, similars, tags, summary, artistName, mbid);

    }

    public List<TrackWithArtistId> getWeeklyBillboard(LastFMData user, int from, int to) throws
            LastFmException {
        List<TrackWithArtistId> list = new ArrayList<>();
        String url = BASE + GET_ALL + user.getName() + apiKey + ENDING + "&extended=1" + "&from=" + (from + 1) + "&to=" + to;

        int page = 0;
        int totalPages = 1;
        while (page < totalPages) {
            String urlPage = url + "&page=" + ++page;
            JSONObject obj = initGetRecentTracks(user, urlPage, new CustomTimeFrame(TimeFrameEnum.WEEK));
            JSONObject attrObj = obj.getJSONObject("@attr");
            totalPages = attrObj.getInt("totalPages");
            if (totalPages == 0) {
                throw new LastFMNoPlaysException(user.getName(), new CustomTimeFrame(TimeFrameEnum.WEEK));
            }

            JSONArray arr = obj.getJSONArray("track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject trackObj = arr.getJSONObject(i);
                if (trackObj.has("@attr"))
                    continue;

                String trackName = trackObj.getString("name");
                String mbid = trackObj.getString("mbid");

                JSONObject artistObj = trackObj.getJSONObject("artist");
                String artistName = artistObj.getString("name");
                String artistMbid = artistObj.getString("mbid");
                boolean loved = trackObj.getInt("loved") != 0;
                int utc = trackObj.getJSONObject("date").getInt("uts");
                TrackWithArtistId track = new TrackWithArtistId(artistName, trackName, 0, loved, 0, utc);
                track.setArtistMbid(artistMbid);
                track.setMbid(mbid);
                JSONObject albumObj = trackObj.optJSONObject("album");

                if (albumObj != null) {
                    track.setAlbum(albumObj.getString("#text"));
                    track.setAlbumMbid(albumObj.getString("mbid"));
                }
                list.add(track);
            }
        }
        return list;
    }

    public int getLastScrobbleUTS(LastFMData user) throws
            LastFmException {
        String url = BASE + RECENT_TRACKS + "&limit=1&user=" + user.getName() + apiKey + ENDING + "&extended=1";

        JSONObject obj = initGetRecentTracks(user, url, new CustomTimeFrame(TimeFrameEnum.ALL));
        JSONObject attrObj = obj.getJSONObject("@attr");
        int totalPages = attrObj.getInt("totalPages");
        if (totalPages == 0) {
            return Math.toIntExact(Instant.now().getEpochSecond());
        }

        JSONArray arr = obj.getJSONArray("track");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject trackObj = arr.getJSONObject(i);
            if (trackObj.has("@attr"))
                continue;
            return trackObj.getJSONObject("date").getInt("uts");
        }
        return Math.toIntExact(Instant.now().getEpochSecond());
    }


    public NPService.NPUpdate getNPWithUpdate(LastFMData user, int from) throws
            LastFmException {

        String url = BASE + GET_ALL + user.getName() + apiKey + ENDING + "&extended=1" + "&from=" + (from - 1);
        // -1 To always include last scrobble, even if we are not playing anything. We have to make sure to not include those in the batch for update
        AtomicInteger page = new AtomicInteger(1);
        AtomicInteger totalPages = new AtomicInteger(1);
        CustomTimeFrame timeFrameEnum = new CustomTimeFrame(TimeFrameEnum.ALL);
        JSONObject methodObj = doMethod(url, new ExceptionEntity(user.getName()), user);
        if (!methodObj.has("recenttracks")) {
            throw new LastFMNoPlaysException(user.getName(), timeFrameEnum);
        }
        JSONObject obj = methodObj.getJSONObject("recenttracks");
        JSONObject attrObj = obj.getJSONObject("@attr");
        JSONArray arr = obj.optJSONArray("track");
        if (arr == null) {
            JSONObject t = obj.getJSONObject("track");
            JSONArray objects = new JSONArray();
            objects.put(t);
            obj.remove("track");
            obj.put("track", objects);
        }
        arr = obj.getJSONArray("track");
        totalPages.set(attrObj.getInt("totalPages"));
        // Since we are asking with from >= now it should always include one scrobble at least. The last one
        if (arr.length() == 0) {
            // This now should never happen.
            Chuu.getLogger().warn("NP is empty for: {} from: {}", user.getName(), from);
            return new NPService.NPUpdate(getNowPlayingInfo(user), CompletableFuture.completedFuture(Collections.emptyList()));
        }
        JSONObject trackObj = arr.getJSONObject(0);
        boolean np = (trackObj.has("@attr"));
        JSONObject artistObj = trackObj.getJSONObject("artist");
        String artistName = artistObj.getString("name");
        String mbid = artistObj.getString("mbid");
        String albumName = trackObj.getJSONObject("album").getString("#text");
        String songName = trackObj.getString("name");
        boolean loved = trackObj.getInt("loved") != 0;
        String imageUrl = obtainImage(trackObj);


        CompletableFuture<List<TrackWithArtistId>> refetchNps = CommandUtil.supplyLog(() -> {
            List<TrackWithArtistId> list = new ArrayList<>();

            if (attrObj.getInt("total") == 0) {
                return Collections.emptyList();
            }
            try {
                if (page.get() == 1) {
                    handleList(obj, list, from);
                }
                while (page.get() < totalPages.get()) {
                    String pag = url + "&page=" + page.incrementAndGet();
                    JSONObject innerObj = initGetRecentTracks(user, pag, timeFrameEnum);
                    handleList(innerObj, list, from);
                }
                return list;
            } catch (Exception e) {
                return Collections.emptyList();
            }
        });
        return new NPService.NPUpdate(new NowPlayingArtist(artistName, mbid, np, albumName, songName, imageUrl, user.getName(), loved), refetchNps);
    }

    private void handleList(JSONObject obj, List<TrackWithArtistId> list, int from) {
        JSONArray arr = obj.getJSONArray("track");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject trackObj = arr.getJSONObject(i);
            if (trackObj.has("@attr"))
                continue;

            int utc = trackObj.getJSONObject("date").getInt("uts");
            //
            if (utc <= from) {
                continue;
            }
            String trackName = trackObj.getString("name");
            boolean loved = trackObj.getInt("loved") != 0;
            String mbid = trackObj.getString("mbid");

            JSONObject artistObj = trackObj.getJSONObject("artist");
            String artistName = artistObj.getString("name");
            String artistMbid = artistObj.getString("mbid");
            TrackWithArtistId track = new TrackWithArtistId(artistName, trackName, 0, loved, 0, utc);
            track.setArtistMbid(artistMbid);
            track.setMbid(mbid);
            JSONObject albumObj = trackObj.optJSONObject("album");

            if (albumObj != null) {
                track.setAlbum(albumObj.getString("#text"));
                track.setAlbumMbid(albumObj.getString("mbid"));
            }
            list.add(track);
        }
    }


    public List<TimestampWrapper<Track>> getTracksAndTimestamps(LastFMData user, int from, int to) throws
            LastFmException {
        List<TimestampWrapper<Track>> list = new ArrayList<>();
        String url = BASE + GET_ALL + user.getName() + apiKey + ENDING + "&extended=1" + "&from=" + (from) + "&to=" + to;

        int page = 0;
        int totalPages = 1;
        while (page < totalPages) {
            String urlPage = url + "&page=" + ++page;
            JSONObject obj = initGetRecentTracks(user, urlPage, new CustomTimeFrame(TimeFrameEnum.WEEK));
            JSONObject attrObj = obj.getJSONObject("@attr");
            totalPages = attrObj.getInt("totalPages");
            if (totalPages == 0) {
                throw new LastFMNoPlaysException(user.getName(), new CustomTimeFrame(TimeFrameEnum.WEEK));
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

    public List<Track> getListTopTrack(LastFMData user, TimeFrameEnum timeframe) throws LastFmException {

        if (timeframe == TimeFrameEnum.DAY) {
            return getDailyT(TopEntity.TRACK, user, capsule -> new Track(capsule.getArtistName(), capsule.getAlbumName(), capsule.getPlays(), false, SONG_AVERAGE_DURATION), Integer.MAX_VALUE);
        }
        List<Track> trackList = new ArrayList<>();
        String url = BASE + GET_TOP_TRACKS + user.getName() + apiKey + ENDING + "&period=" + timeframe.toApiFormat() + "&limit=1000";
        // Execute the method.
        JSONObject obj = doMethod(url, new ExceptionEntity(user.getName()), user);

        obj = obj.getJSONObject("toptracks");
        if (obj.getJSONObject("@attr").getInt("totalPages") == 0)
            throw new LastFMNoPlaysException(user.getName(), new CustomTimeFrame(timeframe));

        JSONArray arr = obj.getJSONArray("track");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject jsonObj = (arr.getJSONObject(i));
            String name = jsonObj.getString("name");
            int duration = parseDuration(jsonObj, true);
            int frequency = jsonObj.getInt("playcount");
            String artist_name = jsonObj.getJSONObject("artist").getString("name");

            trackList.add(new Track(artist_name, name, frequency, false, duration));
        }

        return trackList;

    }


    public FullAlbumEntityExtended getAlbumSummary(LastFMData user, String artist, String album) throws
            LastFmException {
        JSONObject obj = initAlbumJSON(user, artist, album);
        String imageUrl = obtainImage(obj);

        String correctedArtist = obj.getString("artist");
        String correctedAlbum = obj.getString("name");
        String mbid = obj.optString("mbid");

        int playCount = obj.optInt("userplaycount", 0);
        int totalPlayCount = obj.getInt("playcount");
        int listeners = obj.getInt("listeners");

        int duration = 0;
        List<String> tags = parseTags(obj);
        String sumamry = parseBio(obj, "wiki");
        FullAlbumEntityExtended fae = new FullAlbumEntityExtended(correctedArtist, correctedAlbum, playCount, imageUrl, user.getName(), listeners, totalPlayCount, tags, sumamry);
        fae.setMbid(mbid);
        if (obj.has("tracks")) {
            JSONArray arr = getFMArr(obj.getJSONObject("tracks"), "track");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject trackObj = arr.getJSONObject(i);
                String trackName = trackObj.getString("name");
                int trackLength = parseDuration(trackObj, false);
                Track track = new Track(correctedArtist, trackName, 0, false, trackLength);
                track.setPosition(trackObj.getJSONObject("@attr").getInt("rank"));
                duration += trackLength;
                fae.addTrack(track);
            }
        }
        fae.setTotalDuration(duration);
        return fae;
    }


    private int parseDuration(JSONObject root, boolean useAverage) {
        int duration = root.optInt("duration", useAverage ? SONG_AVERAGE_DURATION : 0);
        if (duration == 0 && useAverage) {
            return SONG_AVERAGE_DURATION;
        }
        return duration;
    }

    private List<String> parseTags(JSONObject obj, String parentKey) {
        JSONObject tagsObj = obj.optJSONObject(parentKey);
        if (tagsObj != null) {
            JSONArray tagsArray = getFMArr(tagsObj, "tag");
            return StreamSupport.stream(tagsArray.spliterator(), false).map(JSONObject.class::cast)
                    .map(x -> x.getString("name")).toList();
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> parseTags(JSONObject obj) {
        return parseTags(obj, "tags");
    }

    public TrackExtended getTrackInfoExtended(LastFMData user, String artist, String song) throws
            LastFmException {
        String url = BASE + GET_TRACK_INFO + user.getName() + "&artist=" + URLEncoder
                .encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder
                             .encode(song, StandardCharsets.UTF_8) +
                     apiKey + ENDING + "&autocorrect=1";
        ExceptionEntity exceptionEntity = new TrackException(artist, song);
        JSONObject obj = doMethod(url, exceptionEntity, user);
        obj = obj.getJSONObject("track");

        int userplaycount;
        if (!obj.has("userplaycount")) {
            userplaycount = 0;
        } else {
            userplaycount = obj.getInt("userplaycount");
        }
        int listeners = obj.getInt("listeners");
        int totalPlayCount = obj.getInt("playcount");

        String mbid = obj.optString("mbid");

        String reTrackName = obj.getString("name");
        boolean userloved = obj.has("userloved") && obj.getInt("userloved") != 0;
        int duration = parseDuration(obj, false) / 1000;
        String reArtist = obj.getJSONObject("artist").getString("name");


        List<String> tags = parseTags(obj, "toptags");
        String albumName = null;
        if ((obj).has("album")) {
            albumName = obj.getJSONObject("album").getString("title");
        }
        TrackExtended track = new TrackExtended(reArtist, reTrackName, userplaycount, userloved, duration, tags, totalPlayCount, listeners, albumName);
        track.setMbid(mbid);
        JSONObject images;
        if ((images = obj).has("album") && (images = images.getJSONObject("album")).has("image")) {

            track.setImageUrl(obtainImage(images));
        }
        return track;
    }


    public int scrobblesSince(LastFMData user, OffsetDateTime date) throws LastFmException {
        long time = date.toEpochSecond();
        String url = BASE + GET_NOW_PLAYINH + user.getName() + apiKey + ENDING + "&extended=0&from=" + (time);
        JSONObject obj = initGetRecentTracks(user, url, new CustomTimeFrame(TimeFrameEnum.ALL));
        JSONObject attrObj = obj.getJSONObject("@attr");
        return attrObj.getInt("total");
    }

    public List<String> getUserArtistTags(int count, String artist, LastFMData user, @NotNull String session) throws
            LastFmException {
        String url = BASE + GET_USER_ARTIST_TAGS + "&artist=" +
                     URLEncoder
                             .encode(artist, StandardCharsets.UTF_8) +
                     apiKey + ENDING;

        JSONObject obj = doMethod(url, new ArtistException(artist), user);

        List<String> tags = new ArrayList<>();

        obj = obj.getJSONObject("tags");
        JSONArray tag = obj.optJSONArray("tag");
        if (tag != null) {
            for (
                    int i = 0; i < tag.length() && i < count; i++) {
                tags.add(tag.getJSONObject(i).getString("name"));
            }
        }
        return tags;
    }

    public List<String> getTrackTags(int count, TopEntity entity, String artist, @Nullable String track) throws
            LastFmException {
        String url = "";
        switch (entity) {
            case ALBUM -> {
                assert track != null;
                url = BASE + GET_ALBUM_TAGS + "&artist=" +
                      URLEncoder
                              .encode(artist, StandardCharsets.UTF_8) + "&album=" + URLEncoder
                              .encode(track, StandardCharsets.UTF_8) +
                      apiKey + ENDING + "&autocorrect=1";
            }
            case TRACK -> {
                assert track != null;
                url = BASE + GET_TRACK_TAGS + "&artist=" +
                      URLEncoder
                              .encode(artist, StandardCharsets.UTF_8) + "&track=" + URLEncoder
                              .encode(track, StandardCharsets.UTF_8) +
                      apiKey + ENDING + "&autocorrect=1";
            }
            case ARTIST -> url = BASE + GET_ARTIST_TAGS + "&artist=" +
                                 URLEncoder
                                         .encode(artist, StandardCharsets.UTF_8) +
                                 apiKey + ENDING + "&autocorrect=1";
        }

        JSONObject obj = doMethod(url, new TrackException(artist, track), null);

        obj = obj.getJSONObject("toptags");
        JSONArray tag = obj.getJSONArray("tag");
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tag.length() && i < count; i++) {
            tags.add(tag.getJSONObject(i).getString("name"));
        }
        return tags;
    }

    public GenreInfo getGenreInfo(String genre) throws LastFmException {
        String url = BASE + GET_TAG_INFO + URLEncoder
                .encode(genre, StandardCharsets.UTF_8) + apiKey + ENDING;
        try {
            JSONObject obj = doMethod(url, new ArtistException(genre), null);
            JSONObject tag = obj.getJSONObject("tag");
            String summary = tag.getJSONObject("wiki").getString("content");
            if (!summary.isBlank()) {
                int i = summary.indexOf("<a");
                if (i != -1) {
                    String substring = summary.substring(0, i);
                    if (!substring.trim().endsWith("."))
                        substring += "...";
                    summary = substring;
                }
            }
            return new GenreInfo(tag.getString("name"), tag.getInt("total"), tag.getInt("reach"), summary);
        } catch (LastFmEntityNotFoundException ex) {
            return new GenreInfo(genre, 0, 0, "");
        }
    }

    public int getRangeChartChart(LastFMData userName, long from, long to, int x, int y, TopEntity
            entity, BiFunction<JSONObject, Integer, UrlCapsule> parser, Queue<UrlCapsule> queue, CustomTimeFrame
                                          customTimeFrame) throws LastFmException {
        int requestedSize = x * y;
        if (from < 0) {
            from = 0;
        }
        String apiMethod = entity.getCustomApiMethod();
        String leadingObject = entity.getCustomLeadingObject();
        String arrayObject = entity.getCustomArrayObject();

        String url = BASE + apiMethod + userName.getName() + apiKey + ENDING + "&from=" + (from) + "&to=" + to;
        int size = 0;
        if (requestedSize >= 1000)
            url += "&limit=1000";
        else
            url += "&limit=" + requestedSize;


        // Execute the method.
        JSONObject obj = doMethod(url, new ExceptionEntity(userName.getName()), userName);
        obj = obj.getJSONObject(leadingObject);


        JSONArray arr = obj.getJSONArray(arrayObject);
        for (int i = 0; i < arr.length() && size < requestedSize; i++) {
            JSONObject albumObj = arr.getJSONObject(i);
            queue.add(parser.apply(albumObj, size));
            ++size;
        }
        if (size == 0) {
            throw new LastFMNoPlaysException(userName.getName(), customTimeFrame);
        }
        return size;
    }

    public CountWrapper<List<TrackWithArtistId>> getLovedSongs(LastFMData user) throws LastFmException {
        List<TrackWithArtistId> ret = new ArrayList<>();
        String url = BASE + LOVED_TRACKS + "&user=" + user.getName() + "&limit=1000" + apiKey + ENDING;
        JSONObject obj = doMethod(url, new ExceptionEntity(user.getName()), user);
        obj = obj.getJSONObject("lovedtracks");
        int size = obj.getJSONObject("@attr").getInt("total");
        JSONArray arr = obj.getJSONArray("track");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject trackObj = arr.getJSONObject(i);

            String trackName = trackObj.getString("name");
            String mbid = trackObj.getString("mbid");

            JSONObject artistObj = trackObj.getJSONObject("artist");
            String artistName = artistObj.getString("name");
            String artistMbid = artistObj.getString("mbid");

            int utc = trackObj.getJSONObject("date").getInt("uts");
            TrackWithArtistId track = new TrackWithArtistId(artistName, trackName, 0, true, 0, utc);
            track.setArtistMbid(artistMbid);
            track.setMbid(mbid);
            JSONObject albumObj = trackObj.optJSONObject("album");
            track.setImageUrl(obtainImage(trackObj));
            if (albumObj != null) {
                track.setAlbum(albumObj.getString("#text"));
                track.setAlbumMbid(albumObj.getString("mbid"));
            }
            ret.add(track);
        }
        return new CountWrapper<>(size, ret);
    }

    public Optional<TrackWithArtistId> getMilestone(LastFMData user, long number) throws LastFmException {
        String url = BASE + RECENT_TRACKS + "&user=" + user.getName() + "&limit=1" + apiKey + ENDING + "&extended=1";
        JSONObject obj = initGetRecentTracks(user, url, new CustomTimeFrame(TimeFrameEnum.ALL));
        long pageNumber = obj.getJSONObject("@attr").getLong("total");
        long l = pageNumber - number + 1;
        if (l < 0) {
            return Optional.empty();
        }
        url = BASE + RECENT_TRACKS + "&user=" + user.getName() + "&limit=1" + apiKey + ENDING + "&extended=1&page=" + l;
        obj = initGetRecentTracks(user, url, new CustomTimeFrame(TimeFrameEnum.ALL));
        JSONArray arr = obj.getJSONArray("track");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject trackObj = arr.getJSONObject(i);
            if (trackObj.has("@attr"))
                continue;

            String trackName = trackObj.getString("name");
            String mbid = trackObj.getString("mbid");

            JSONObject artistObj = trackObj.getJSONObject("artist");
            String artistName = artistObj.getString("name");
            String artistMbid = artistObj.getString("mbid");

            int utc = trackObj.getJSONObject("date").getInt("uts");
            TrackWithArtistId track = new TrackWithArtistId(artistName, trackName, 0, false, 0, utc);
            track.setArtistMbid(artistMbid);
            track.setMbid(mbid);
            JSONObject albumObj = trackObj.optJSONObject("album");
            track.setImageUrl(obtainImage(trackObj));
            if (albumObj != null) {
                track.setAlbum(albumObj.getString("#text"));
                track.setAlbumMbid(albumObj.getString("mbid"));
            }
            return Optional.of(track);
        }
        return Optional.empty();
    }

    public String getAuthToken() throws LastFmException {
        String url = getSignature(BASE + GET_TOKEN + apiKey + ENDING);
        return doMethod(url, null, null).getString("token");
    }

    public String getAuthSession(LastFMData lastFMData) throws LastFmException {
        String url = getSignature(BASE + GET_SESSION + lastFMData.getToken() + apiKey) + ENDING;
        return doMethod(url, null, LastFMData.ofUser(lastFMData.getName())).getJSONObject("session").getString("key");
//        return doMethod(url, null).ge("token");


    }

    private String getSignature(String url) {
        try {
            List<NameValuePair> parse = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
            NameValuePair pair = generateHash(parse);
            return url + String.format("&%s=%s", pair.getName(), pair.getValue()) + ENDING;
        } catch (URISyntaxException e) {
            throw new ChuuServiceException(e);
        }
    }

    private String getSignature(PostEntity scrobblePost) {
        List<? extends NameValuePair> items = Arrays.stream(scrobblePost.getClass().getRecordComponents()).map(x -> {
            try {
                Object invoked = x.getAccessor().invoke(scrobblePost);
                if (invoked == null) {
                    return null;
                }
                return new BasicNameValuePair(x.getName(), invoked.toString());
            } catch (IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();

        NameValuePair apiSig = generateHash(items);
        return Stream.concat(items.stream(), Stream.of(apiSig)).map(key -> key.getName() + "=" + URLEncoder.encode(key.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

    }

    private NameValuePair generateHash(List<? extends NameValuePair> items) {
        String preHash = items.stream().filter(x -> !x.getName().startsWith("format")).sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName())).map(x -> x.getName() + x.getValue()).collect(Collectors.joining()) + secret;

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ChuuServiceException(e);
        }
        md5.update(StandardCharsets.UTF_8.encode(preHash));
        String hash = String.format("%032x", new BigInteger(1, md5.digest()));
        return new BasicNameValuePair("api_sig", hash);

    }

    public String generateAuthoredCall(String url, String session) {
        return getSignature(url + "&sk=" + session);
    }

    public String generateSessionUrl(String token) {
        return String.format("http://www.last.fm/api/auth/?token=%s%s", token, apiKey);
    }

    public String findSession(String token) throws LastFmException {
        String sess = getSignature(BASE + GET_SESSION + token + apiKey) + ENDING;
        return doMethod(sess, null, null).getJSONObject("session").getString("key");
    }

    public String findUserAccount(String session) throws LastFmException {
        String url = BASE + "?method=user.getinfo" + apiKey + ENDING;
        String s = generateAuthoredCall(url, session);
        return doMethod(s, null, null).getJSONObject("user").getString("name");
    }

    public void scrobble(String sessionKey, Scrobble scrobble, Instant instant) throws LastFmException {
        ScrobblePost scrobblePost = new ScrobblePost("track.scrobble", scrobble.artist(), scrobble.song(), scrobble.album(), null, null, instant.getEpochSecond(), null, null, apiKey.split("=")[1], sessionKey);
        JSONObject jsonObject = doPost(scrobblePost);

    }

    public void love(String sessionKey, Scrobble scrobble) throws LastFmException {
        PostEntity scrobblePost = new LovePost("track.love", scrobble.artist(), scrobble.song(), apiKey.split("=")[1], sessionKey);
        JSONObject jsonObject = doPost(scrobblePost);
    }

    public void unlove(String sessionKey, Scrobble scrobble) throws LastFmException {
        PostEntity scrobblePost = new LovePost("track.unlove", scrobble.artist(), scrobble.song(), apiKey.split("=")[1], sessionKey);
        JSONObject jsonObject = doPost(scrobblePost);
    }

    public void flagNP(String sessionKey, Scrobble scrobble) throws LastFmException {
        Integer duration = scrobble.duration() == null ? null : Math.toIntExact(scrobble.duration() / 1000);
        ScrobblePost scrobblePost = new ScrobblePost("track.updateNowPlaying", scrobble.artist(), scrobble.song(), scrobble.album(), null, null, null, duration, null, apiKey.split("=")[1], sessionKey);
        JSONObject jsonObject = doPost(scrobblePost);
    }

    private JSONObject doPost(PostEntity post) throws LastFmException {
        HttpRequest method = createPost(post);
        int counter = 0;
        while (true) {
            try {
                Chuu.incrementMetric();

                CompletableFuture<HttpResponse<InputStream>> cf = client.sendAsync(method, HttpResponse.BodyHandlers.ofInputStream());
                HttpResponse<InputStream> send = cf.get(5, TimeUnit.SECONDS);
                VirtualParallel.handleInterrupt();
                int responseCode = send.statusCode();
                parseHttpCode(responseCode);
                JSONObject jsonObject;
                if (responseCode == 404) {
                    throw new LastFmEntityNotFoundException(null);
                }
                try (InputStream responseBodyAsStream = send.body()) {
                    jsonObject = new JSONObject(new JSONTokener(responseBodyAsStream));
                } catch (JSONException exception) {
                    VirtualParallel.handleInterrupt();
                    Chuu.getLogger().warn(exception.getMessage(), exception);
                    Chuu.getLogger().warn("JSON Exception doing url: {}, code: {}, ", method.uri(), responseCode);
                    throw new ChuuServiceException(exception);
                }
                if (jsonObject.has("error")) {
                    parseResponse(jsonObject, null, null);
                }
                if (Math.floor((float) responseCode / 100) == 4) {
                    Chuu.getLogger().warn("Error {} with url {}", responseCode, method.uri().toString());
                    throw new UnknownLastFmException(jsonObject.toString(), responseCode, null);
                }

                return jsonObject;
            } catch (InterruptedException e) {
                VirtualParallel.handleInterrupt();
                Chuu.getLogger().warn(e.getMessage(), e);
                throw new ChuuServiceException(e);
            } catch (IOException | LastFMServiceException | ExecutionException | TimeoutException e) {
                VirtualParallel.handleInterrupt();
                if (e instanceof LastFMServiceException) {
                    Chuu.getLogger().warn(method.uri().toString());
                    Chuu.getLogger().warn("LAST.FM Internal Error");
                }
                Chuu.getLogger().warn(e.getMessage(), e);
            }
            if (++counter == 2) {
                throw new LastFMConnectionException("500");
            }

        }

    }

    private String parseBio(JSONObject root) {
        return parseBio(root, "bio");
    }

    private String parseBio(JSONObject root, String key) {
        JSONObject bio = root.optJSONObject(key);
        String summary;
        if (bio != null) {
            String field = bio.getString("summary");
            int i = field.indexOf("<a");
            summary = field.substring(0, i);
        } else {
            summary = "";
        }
        return summary;
    }


}



