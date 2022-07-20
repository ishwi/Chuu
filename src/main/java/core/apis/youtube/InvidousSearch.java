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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvidousSearch implements YoutubeSearch {
    public static final List<String> domains = List.of("invidious.snopyta.org", "invidious.zapashcanon.fr", "invidiou.site", "yewtu.be", "tube.connect.cafe", "vid.mint.lgbt");
    public static final List<String> regions = List.of("us", "nz", "es", "fr");
    private static final String BASE_ENDPOINT = "https://";
    private static final String SEARCH_ENDPOINT = "/api/v1/search";
    private final HttpClient httpClient;

    public InvidousSearch() {
        httpClient = ClientSingleton.getInstance();
    }

    private String search(String queryTerm, int retries, Set<String> retried, Set<String> regionsRetried) {
        if (retries == 0) {
            return "";
        }
        List<String> domains = InvidousSearch.domains.stream().filter(x -> !retried.contains(x)).toList();
        List<String> regions = InvidousSearch.regions.stream().filter(x -> !regionsRetried.contains(x)).toList();
        String domain = domains.get(CommandUtil.rand.nextInt(domains.size()));
        String region = regions.get(CommandUtil.rand.nextInt(regions.size()));
        retried.add(domain);
        regionsRetried.add(region);
        String api = BASE_ENDPOINT + domain + SEARCH_ENDPOINT;
        String q = api + "?q=" + URLEncoder
                .encode(queryTerm, StandardCharsets.UTF_8) + "&fields=videoId&region=" + region;
        String responseUrl = "";
        try {
            HttpRequest build = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(q))
                    .setHeader("User-Agent", "discordBot/ishwi6@gmail.com") // add request header
                    .build();

            HttpResponse<InputStream> send = httpClient.send(build, HttpResponse.BodyHandlers.ofInputStream());
            int responseCode = send.statusCode();
            parseHttpCode(responseCode);
            if (responseCode != 200) {
                return search(queryTerm, retries - 1, retried, regionsRetried);
            }
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
            Chuu.getLogger().info(exception.getMessage(), exception);
        }
        return responseUrl;

    }

    @Override
    public String doSearch(String queryTerm) {
        return search(queryTerm, 3, new HashSet<>(), new HashSet<>());

    }

    private void parseHttpCode(int responseCode) {
        // TODO
    }
}
