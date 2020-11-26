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
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import core.Chuu;
import dao.entities.ScrobbledTrack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
            Chuu.getLogger().info("Spotify Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
    }


    private void initRequest() {
        if (!this.time.isAfter(LocalDateTime.now())) {
            clientCredentialsSync();
        }
    }

    private Paging<AlbumSimplified> searchAlbum(String artist, String album) throws ParseException, SpotifyWebApiException, IOException {
        initRequest();
        artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
        album = album.contains(":") ? "\"" + album + "\"" : album;

        SearchAlbumsRequest build = spotifyApi.searchAlbums("album:" + album + " artist:" + artist).
                market(CountryCode.NZ)
                .limit(1)
                .offset(0)
                .build();
        return build.execute();
    }

    private Paging<Track> searchSong(String artist, String track) throws ParseException, SpotifyWebApiException, IOException {
        initRequest();
        artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
        track = track.contains(":") ? "\"" + track + "\"" : track;

        SearchTracksRequest build = spotifyApi.searchTracks("track:" + track + " artist:" + artist).
                market(CountryCode.NZ)
                .limit(1)
                .offset(0)
                .build();
        return build.execute();
    }

    public List<Pair<ScrobbledTrack, Track>> searchMultipleTracks(List<ScrobbledTrack> scrobbledTracks) {
        return scrobbledTracks.stream().map(x -> {
            try {
                Paging<Track> trackPaging = searchSong(x.getArtist(), x.getName());
                if (trackPaging.getItems().length == 0)
                    return null;
                return Pair.of(x, trackPaging.getItems()[0]);
            } catch (ParseException | SpotifyWebApiException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String getAlbumLink(String artist, String album) {
        String returned = null;
        try {
            Paging<AlbumSimplified> albumSimplifiedPaging = searchAlbum(artist, album);
            for (AlbumSimplified item : albumSimplifiedPaging.getItems()) {
                returned = "https://open.spotify.com/album/" + item.getUri().split("spotify:album:")[1];
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return returned;

    }

    public List<AudioFeatures> getAudioFeatures(Set<String> ids) {
        List<AudioFeatures> audioFeatures = new ArrayList<>();
        initRequest();
        if (ids.isEmpty()) {
            return audioFeatures;
        }
        for (int i = 0; i < 5; i++) {
            String[] strings = ids.stream().skip(i * 100).limit(100).toArray(String[]::new);
            if (ids.size() < i * 100) {
                break;
            }
            GetAudioFeaturesForSeveralTracksRequest build = spotifyApi.getAudioFeaturesForSeveralTracks(strings).build();
            try {
                AudioFeatures[] execute = build.execute();
                audioFeatures.addAll(Arrays.asList(execute));
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }
        }


        return audioFeatures;


    }

    public List<dao.entities.Track> getAlbumTrackList(String artist, String album) {
        ArrayList<dao.entities.Track> tracks = new ArrayList<>();
        String returned = "";
        try {
            Paging<AlbumSimplified> albumSimplifiedPaging = searchAlbum(artist, album);
            for (AlbumSimplified item : albumSimplifiedPaging.getItems()) {
                returned = item.getId();
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        if (returned.equals("")) {
            return tracks;
        }
        try {
            return Arrays.stream(spotifyApi.getAlbum(returned).market(CountryCode.NZ).build().execute().getTracks().getItems()).map(x -> {
                dao.entities.Track track = new dao.entities.Track(artist, x.getName(), 0, false, x.getDurationMs());
                track.setPosition(x.getTrackNumber() - 1);
                return track;
            }).collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return tracks;
        }

    }

    public String searchItems(String track, String artist, String album) {
        initRequest();
        SearchItemRequest tracksRequest =
                spotifyApi.searchItem("track:" + track + " artist:" + artist, "track").limit(1)
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
        Artist[] artists = searchArtist(artist);
        if (artist == null) {
            return "";
        }
        for (Artist item : artists) {
            Image[] images = item.getImages();
            if (images.length != 0)
                return images[0].getUrl();
        }
        return "";
    }

    public Pair<String, String> getUrlAndId(String artist) {
        Artist[] artists = searchArtist(artist);
        if (artist == null) {
            return Pair.of("", "");
        }
        for (Artist item : artists) {
            Image[] images = item.getImages();
            if (images.length != 0)
                return Pair.of(images[0].getUrl(), item.getId());
        }
        return Pair.of("", "");
    }

    private Artist[] searchArtist(String artist) {
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
            return searchResult.getArtists().getItems();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }

}
