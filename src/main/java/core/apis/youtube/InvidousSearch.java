package core.apis.youtube;

import core.Chuu;
import core.apis.ClientSingleton;
import core.commands.utils.CommandUtil;
import dao.exceptions.ChuuServiceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InvidousSearch implements YoutubeSearch {
    public static final List<String> domains = List.of("invidious.snopyta.org", "invidious.xyz");
    private static final String BASE_ENDPOINT = "https://";
    private static final String SEARCH_ENDPOINT = "/api/v1/search";
    private final HttpClient httpClient;

    public InvidousSearch() {
        httpClient = ClientSingleton.getInstance();
    }

    @Override
    public String doSearch(String queryTerm) {
        String responseUrl = "";
        String api = BASE_ENDPOINT + domains.get(CommandUtil.rand.nextInt(domains.size())) + SEARCH_ENDPOINT;
        String q = api + "?q=" + URLEncoder
                .encode(queryTerm, StandardCharsets.UTF_8) + "&fields=videoId";
        try {
            HttpRequest build = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(q))
                    .setHeader("User-Agent", "discordBot/ishwi6@gmail.com") // add request header
                    .build();

            HttpResponse<InputStream> send = httpClient.send(build, HttpResponse.BodyHandlers.ofInputStream());
            int responseCode = send.statusCode();
            parseHttpCode(responseCode);
            JSONArray jsonObject;
            try (InputStream responseBodyAsStream = send.body()) {
                jsonObject = new JSONArray(new JSONTokener(responseBodyAsStream));
            } catch (JSONException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                throw new ChuuServiceException(exception);
            }
            if (jsonObject.length() > 0) {
                responseUrl = "https://www.youtube.com/watch?v=" + jsonObject.getJSONObject(0).getString("videoId");
            }
        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
        return responseUrl;

    }

    private void parseHttpCode(int responseCode) {
        // TODO
    }
}
