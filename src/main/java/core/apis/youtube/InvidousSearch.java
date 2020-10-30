package core.apis.youtube;

import core.Chuu;
import core.apis.ClientSingleton;
import core.commands.CommandUtil;
import dao.exceptions.ChuuServiceException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
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
        GetMethod method = new GetMethod(q);
        try {
            int responseCode = httpClient.executeMethod(method);
            parseHttpCode(responseCode);
            JSONArray jsonObject;
            try (InputStream responseBodyAsStream = method.getResponseBodyAsStream()) {
                jsonObject = new JSONArray(new JSONTokener(responseBodyAsStream));
            } catch (JSONException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                throw new ChuuServiceException(exception);
            }
            if (jsonObject.length() > 0) {
                responseUrl = Search.BASE_URL + jsonObject.getJSONObject(0).getString("videoId");
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return responseUrl;

    }

    private void parseHttpCode(int responseCode) {

    }
}
