package core.apis.lyrics;

import core.Chuu;
import core.apis.ClientSingleton;
import dao.exceptions.ChuuServiceException;
import org.apache.commons.text.StringEscapeUtils;
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

public class LyricsApi {
    private static final String BASE_ENDPOINT = "https://lyrics.tsu.sh/v1/?q=";
    private final HttpClient httpClient;

    public LyricsApi() {
        httpClient = ClientSingleton.getInstance();
    }

    public Optional<Lyrics> getLyrics(String queryTerm) {
        String q = BASE_ENDPOINT + URLEncoder
                .encode(queryTerm, StandardCharsets.UTF_8);
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

            String content = StringEscapeUtils.unescapeHtml4(jsonObject.getString("content"));
            JSONObject songObj = jsonObject.getJSONObject("song");
            String fullTitle = songObj.getString("full_title");
            String url = songObj.optString("icon");
            if (url == null) {
                url = jsonObject.getJSONObject("album").optString("icon");
            }
            String[] split = fullTitle.split(" by ");
            assert split.length > 2;
            StringBuilder song = new StringBuilder();
            for (int i = 0; i < split.length - 1; i++) {
                song.append(split[i]).append(" ");
            }

            return Optional.of(new Lyrics(content, song.toString().trim(), split[split.length - 1], url));

        } catch (IOException | InterruptedException exception) {
            throw new ChuuServiceException(exception);
        }

    }


}
