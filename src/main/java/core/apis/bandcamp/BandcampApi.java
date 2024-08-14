package core.apis.bandcamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import core.Chuu;
import core.apis.ClientSingleton;
import dao.exceptions.ChuuServiceException;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BandcampApi {
    private static final String BASE_URL = "https://bandcamp.com/api/";
    private final HttpClient http;

    public BandcampApi() {
        http = ClientSingleton.getInstance();
    }

    public List<Result> discoverReleases(List<String> releases) {
        DiscoverPayload payload = new DiscoverPayload(releases);

        ObjectWriter objectWriter = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .writerFor(DiscoverPayload.class);

        String body = null;
        try {
            body = objectWriter.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpRequest req = HttpRequest.newBuilder()
                .setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .uri(URI.create(BASE_URL + "discover/1/discover_web"))
                .setHeader("User-Agent", "discordBot/ishwi6@gmail.com") // add request header
                .build();
        HttpResponse<byte[]> send = null;
        try {
            send = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            int responseCode = send.statusCode();
            if (responseCode == 404) {
                //
            }
            JSONObject jsonObject = new JSONObject(new JSONTokener(new ByteArrayInputStream(send.body())));
            JSONArray results = jsonObject.getJSONArray("results");
            int length = results.length();
            List<Result> datas = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                JSONObject result = results.getJSONObject(i);
                //2023-04-08 00:00:00 UTC

                JSONArray tags = result.optJSONArray("item_tags");
                long itemImageId = result.getLong("item_image_id");
                String url = "https://f4.bcbits.com/img/a%d_2.jpg".formatted(itemImageId);
                Result data = new Result(result.getLong("id"),
                        result.getString("title"),
                        result.getString("item_url"),
                        result.getString("result_type"),
                        url,
                        result.getLong("band_id"),
                        result.getString("band_name"),
                        result.getString("band_url"),
                        result.getLong("band_genre_id"),
                        result.optString("band_location"),
                        result.optInt("track_count", 0),
                        result.getDouble("item_duration"),
                        null,
                        result.optString("label_name"),
                        result.optString("label_url"),
                        LocalDate.parse(result.getString("release_date").substring(0, 10))

                );
                datas.add(data);
            }
            return datas;
        } catch (JSONException exception) {
            Chuu.getLogger().warn(exception.getMessage(), exception);
            throw new ChuuServiceException(exception);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    enum Slice {TOP, NEW}

    record DiscoverPayload(long categoryId, List<String> tagNormNames, long geonameId,
                           String slice, String cursor, int size, List<String> includeResultTypes) {

        DiscoverPayload(List<String> tagNormNames) {
            this(0, tagNormNames, 0, "new", "*", 60, List.of("a"));
        }
    }

    public record Result(long id, String title, String url, String resultType, String imageUrl, long bandId,
                         String artistName,
                         String artistImage, long genreId, String location, int trackCount, double duration,
                         List<String> itemTags, String labelName, String labelUrl, LocalDate releaseDay) {

    }

}
