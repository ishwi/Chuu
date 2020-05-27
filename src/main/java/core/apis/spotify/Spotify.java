package core.apis.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.SearchItemRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import core.Chuu;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Spotify {

    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private LocalDateTime time;

    public Spotify(String clientSecret, String clientId) {
        SpotifyApi tempItem = new SpotifyApi.Builder()
                .setClientId(clientId).setClientSecret(clientSecret).build();
        this.clientCredentialsRequest = tempItem.clientCredentials().build();

        this.spotifyApi = tempItem;

        clientCredentialsSync();


    }

    private void clientCredentialsSync() {
        try {
            ClientCredentials clientCredentials = this.clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            this.time = LocalDateTime.now().plusSeconds(clientCredentials.getExpiresIn() - 140L);

            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void initRequest() {
        if (!this.time.isAfter(LocalDateTime.now())) {
            clientCredentialsSync();
        }
    }

    public List<dao.entities.Track> getAlbumTrackList(String artist, String album) {
        initRequest();
        artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
        album = album.contains(":") ? "\"" + album + "\"" : album;

        SearchAlbumsRequest build = spotifyApi.searchAlbums("album:" + album + " artist:" + artist).
                market(CountryCode.NZ)
                .limit(1)
                .offset(0)
                .build();
        String returned = "";
        try {
            Paging<AlbumSimplified> execute = build.execute();
            for (AlbumSimplified item : execute.getItems()) {
                returned = item.getId();
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        if (returned.equals("")) {
            return null;
        }
        String finalArtist = artist;
        String finalAlbum = album;
        try {
            return Arrays.stream(spotifyApi.getAlbum(returned).market(CountryCode.NZ).build().execute().getTracks().getItems()).map(x -> {
                dao.entities.Track track = new dao.entities.Track(finalArtist, x.getName(), 0, false, x.getDurationMs());
                track.setPosition(x.getTrackNumber() - 1);
                return track;
            }).collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return new ArrayList<>();
        }

    }

    public String searchItems(String track, String artist, String album) {
        initRequest();
        artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
        SearchItemRequest tracksRequest =
                spotifyApi.searchItem("album:" + album + " artist:" + artist + " track:" + track, "album,artist,track").
                        market(CountryCode.NZ)
                        .limit(1)
                        .offset(0)
                        .build();
        String returned = "";
        try {
            SearchResult searchResult = tracksRequest.execute();

            for (Track item : searchResult.getTracks().getItems()) {
                returned = "https://open.spotify.com/track/" + item.getUri().split("spotify:track:")[1];
            }
            return returned;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return returned;
    }

    public String getArtistUrlImage(String artist) {
        initRequest();
        artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
        SearchItemRequest tracksRequest =
                spotifyApi.searchItem(" artist:" + artist, "artist").
                        market(CountryCode.NZ)
                        .limit(1)
                        .offset(0)
                        .build();
        String returned = "";
        try {
            SearchResult searchResult = tracksRequest.execute();

            for (Artist item : searchResult.getArtists().getItems()) {
                Image[] images = item.getImages();
                if (images.length != 0)
                    returned = images[0].getUrl();
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return returned;
    }
}
