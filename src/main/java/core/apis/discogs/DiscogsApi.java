package core.apis.discogs;


import core.exceptions.DiscogsServiceException;
import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DiscogsApi {
    private static final String BASE_API = "https://api.discogs.com/";
    private final String secret;
    private final String key;
    private final HttpClient httpClient;
    private boolean slowness = false;

    public DiscogsApi(String secret, String key) {
        this.key = key;
        this.secret = secret;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .priority(1)
                .build();

    }


    private int doSearch(String query) throws DiscogsServiceException {

        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_API + "database/search?q=" + query + "&type=artist&key=" + key + "&secret=" + secret;

        JSONObject obj = doMethod(url);

        JSONArray results = obj.getJSONArray("results");
        int id = 0;
        for (int i = 0; i < results.length(); i++) {
            JSONObject resultObj = results.getJSONObject(i);

            if (new LevenshteinDetailedDistance()
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
        albumenc = URLEncoder.encode(album, StandardCharsets.UTF_8);
        artistenc = URLEncoder.encode(artist, StandardCharsets.UTF_8);

        String url = BASE_API + "database/search?&type=release&artist=" + artistenc + "&release_title=" + albumenc + "&key=" + key + "&secret=" + secret;
        JSONObject obj = doMethod(url);
        JSONArray results = obj.getJSONArray("results");
        String expected = artist + " - " + album;
        for (int i = 0; i < results.length(); i++) {
            JSONObject resultObj = results.getJSONObject(i);

            if (resultObj.has("year") && new LevenshteinDetailedDistance().apply(
                            expected.toLowerCase(), resultObj.getString("title").toLowerCase().replaceAll("\\s\\(\\d+\\)", ""))
                                                 .getDistance() < expected.length() / 2) {
                return Year.of(resultObj.getInt("year"));
            }
        }
        return null;
    }

    private JSONObject doMethod(String url) throws DiscogsServiceException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("User-Agent", "discordArtistImageFetcher/ishwi6@gmail.com") // add request header
                .build();
        try {
            if (slowness) {
                TimeUnit.SECONDS.sleep(1);
            }

            HttpResponse<InputStream> send = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            parseHttpCode(send.statusCode());
            send.headers().firstValueAsLong("X-Discogs-Ratelimit-Remaining").ifPresent(rated -> slowness = rated == 0L);
            return new JSONObject(new JSONTokener(send.body()));

        } catch (IOException | InterruptedException e) {
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
        String url = BASE_API + "artists/" + id + "?key=" + key + "&secret=" + secret;
        JSONObject obj = doMethod(url);
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
            if (obj1 == obj2) {
                return 0;
            }
            float height = obj1.getInt("height");
            float width = obj1.getInt("width");
            float height2 = obj2.getInt("height");
            float width2 = obj2.getInt("width");

            if (((height + width) / (Float.max(height, width) / Float
                    .min(height, width))) <= ((height2 + width2) / (Float.max(height2, width2) / Float
                    .min(height2, width2)))) {
                return -1;
            } else {
                return 1;
            }
        });
        if (opt.isPresent())
            imageUrl = opt.get().getString("resource_url");

        return imageUrl;
    }

    public String findArtistImage(String artist) throws DiscogsServiceException {
        return doArtistInfo(doSearch(artist));
    }
}
