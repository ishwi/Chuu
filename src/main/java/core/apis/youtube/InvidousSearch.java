package core.apis.youtube;

import core.Chuu;
import core.apis.ClientSingleton;
import core.apis.last.exceptions.ExceptionEntity;
import core.exceptions.ChuuServiceException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.UnknownLastFmException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class InvidousSearch implements YoutubeSearch {

    private static final String BASE_ENDPOINT = "https://invidio.us";
    private final HttpClient httpClient;
    private final String SEARCH_ENDPOINT = BASE_ENDPOINT + "/api/v1/search";

    public InvidousSearch() {
        httpClient = ClientSingleton.getInstance();
    }

    @Override
    public String doSearch(String queryTerm) {
        String responseUrl = "";

        String q = SEARCH_ENDPOINT + "?q=" + URLEncoder
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
