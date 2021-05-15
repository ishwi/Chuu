package core.apis.lyrics;

import core.Chuu;
import core.apis.ClientSingleton;
import dao.exceptions.ChuuServiceException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
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
import java.util.Optional;

public class EvanLyrics {
    private static final String BASE_ENDPOINT = "https://evan.lol/";
    private static final String TOP = "lyrics/search/top?q=";

    private final HttpClient httpClient;

    public EvanLyrics() {
        httpClient = ClientSingleton.getInstance();
    }


    @NotNull
    private Optional<Lyrics> doLyrics(String q) {
        try {
            HttpRequest build = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(q))
                    .setHeader("User-Agent", "https://github.com/ishwi/discordBot/")
                    .build();

            HttpResponse<InputStream> send = httpClient.send(build, HttpResponse.BodyHandlers.ofInputStream());
            int responseCode = send.statusCode();
            if (responseCode != 200) {
                return Optional.empty();
            }
            JSONObject jsonObject;
            try (InputStream responseBodyAsStream = send.body()) {
                jsonObject = new JSONObject(new JSONTokener(responseBodyAsStream));
            } catch (JSONException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                throw new ChuuServiceException(exception);
            }

            String content = jsonObject.getString("lyrics");
            String songName = jsonObject.optString("title");
            String artistName = jsonObject.optString("artist");

            return Optional.of(new Lyrics(content, songName, artistName));

        } catch (IOException | InterruptedException exception) {
            throw new ChuuServiceException(exception);
        }
    }

    public Optional<Lyrics> getLyrics(String artist, String song) {
        String q = BASE_ENDPOINT + "lyrics/" + URLEncoder
                .encode(artist.replaceAll(" ", "-"), StandardCharsets.UTF_8) + "/" + URLEncoder
                           .encode(song.replaceAll(" ", "-"), StandardCharsets.UTF_8);
        return doLyrics(q);
    }

    public Optional<Lyrics> getTopResult(String query) {
        return doLyrics(BASE_ENDPOINT + TOP + URLEncoder
                .encode(query.trim(), StandardCharsets.UTF_8));
    }


}

