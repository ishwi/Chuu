package se.michaelthelin.spotify;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import core.apis.ClientSingleton;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadGatewayException;
import se.michaelthelin.spotify.exceptions.detailed.BadRequestException;
import se.michaelthelin.spotify.exceptions.detailed.ForbiddenException;
import se.michaelthelin.spotify.exceptions.detailed.InternalServerErrorException;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;
import se.michaelthelin.spotify.exceptions.detailed.ServiceUnavailableException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;

public class IHttpManagerImpl implements IHttpManager {

    private final HttpClient httpClient;
    private final SpotifyHttpManager delegate;

    public IHttpManagerImpl() {
        httpClient = ClientSingleton.getInstance();
        delegate = new SpotifyHttpManager.Builder().build();
    }

    private String getResponseBody(HttpResponse<String> httpResponse) throws
            IOException,
            SpotifyWebApiException,
            ParseException {

        final String responseBody = httpResponse.body();

        SpotifyApi.LOGGER.log(
                Level.FINE,
                "The http response has body " + responseBody);
        String errorMessage = "some error";

        if (responseBody != null && !responseBody.equals("")) {
            try {
                final JsonElement jsonElement = JsonParser.parseString(responseBody);
                if (jsonElement.isJsonObject()) {
                    final JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (jsonObject.has("error")) {
                        if (jsonObject.has("error_description")) {
                            errorMessage = jsonObject.get("error_description").getAsString();
                        } else if (jsonObject.get("error").isJsonObject() && jsonObject.getAsJsonObject("error").has("message")) {
                            errorMessage = jsonObject.getAsJsonObject("error").get("message").getAsString();
                        }
                    }
                }
            } catch (JsonSyntaxException e) {
                // Not necessary
            }
        }

        SpotifyApi.LOGGER.log(
                Level.FINE,
                "The http response has status code " + httpResponse.statusCode());

        switch (httpResponse.statusCode()) {
            case HttpStatus.SC_BAD_REQUEST:
                throw new BadRequestException(errorMessage);
            case HttpStatus.SC_UNAUTHORIZED:
                throw new UnauthorizedException(errorMessage);
            case HttpStatus.SC_FORBIDDEN:
                throw new ForbiddenException(errorMessage);
            case HttpStatus.SC_NOT_FOUND:
                throw new NotFoundException(errorMessage);
            case 429: // TOO_MANY_REQUESTS (additional status code, RFC 6585)
                // Sets "Retry-After" header as described at https://beta.developer.spotify.com/documentation/web-api/#rate-limiting
                var header = httpResponse.headers().firstValue("Retry-After");

                if (header.isPresent()) {
                    throw new TooManyRequestsException(errorMessage, Integer.parseInt(header.get()));
                } else {
                    throw new TooManyRequestsException(errorMessage);
                }
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                throw new InternalServerErrorException(errorMessage);
            case HttpStatus.SC_BAD_GATEWAY:
                throw new BadGatewayException(errorMessage);
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                throw new ServiceUnavailableException(errorMessage);
            default:
                return responseBody;
        }
    }

    @Override
    public String get(URI uri, Header[] headers) throws IOException, SpotifyWebApiException, ParseException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .GET()
                .timeout(Duration.ofSeconds(5))
                .uri(uri);

        for (Header header : headers) {
            request.setHeader(header.getName(), header.getValue());
        }
        try {
            HttpResponse<String> send = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return this.getResponseBody(send);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String post(URI uri, Header[] headers, HttpEntity body) throws IOException, SpotifyWebApiException, ParseException {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> {
                    try {
                        return body.getContent();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .timeout(Duration.ofSeconds(5))
                .uri(uri);

        for (Header header : headers) {
            request.setHeader(header.getName(), header.getValue());
        }
        try {
            HttpResponse<String> send = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return this.getResponseBody(send);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String put(URI uri, Header[] headers, HttpEntity body) throws IOException, SpotifyWebApiException, ParseException {
        return delegate.put(uri, headers, body);
    }

    @Override
    public String delete(URI uri, Header[] headers, HttpEntity body) throws IOException, SpotifyWebApiException, ParseException {
        return delegate.delete(uri, headers, body);

    }
}
