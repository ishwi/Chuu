package core.apis.spotify;

import com.neovisionaries.i18n.CountryCode;
import core.Chuu;
import dao.entities.ScrobbledTrack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.special.SearchResult;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.SearchItemRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    public List<AlbumResult> searchAlbums(String artist, String album) {
        try {
            return privateSearchAlbums(artist, album);
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            Chuu.getLogger().warn("Error getting tracklist {} | {}", artist, album, e);
            return Collections.emptyList();
        }
    }

    private List<AlbumResult> privateSearchAlbums(String artist, String album) throws ParseException, SpotifyWebApiException, IOException {
        initRequest();
        artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
        album = album.contains(":") ? "\"" + album + "\"" : album;

        SearchAlbumsRequest build = spotifyApi.searchAlbums("album:" + album + " artist:" + artist).
                market(CountryCode.NZ)
                .limit(25)
                .offset(0)
                .build();
        return Arrays.stream(build.execute().getItems())
                .map(result -> new AlbumResult(result.getId(), result.getArtists()[0].getName(), result.getName(), result.getUri(),
                        Arrays.stream(result.getImages()).findFirst().map(Image::getUrl).orElse(null)))
                .distinct()
                .toList();
    }

    public String getAlbumLink(String artist, String album) {
        String returned = null;
        try {
            List<AlbumResult> albumResults = privateSearchAlbums(artist, album);
            for (AlbumResult item : albumResults) {
                returned = "https://open.spotify.com/album/" + item.uri.split("spotify:album:")[1];
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return returned;

    }

    private Paging<Track> searchSong(String artist, String track) throws ParseException, SpotifyWebApiException, IOException {
        initRequest();
        artist = artist.contains(":") ? "\"%s\"".formatted(artist) : artist;
        track = track.contains(":") ? "\"%s\"".formatted(track) : track;

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
                Chuu.getLogger().info(e.getMessage(), e);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    public Optional<dao.entities.Album> findAlbum(String artist, String song) {
        try {
            Paging<Track> trackPaging = searchSong(artist, song);
            if (trackPaging.getItems().length == 0)
                return Optional.empty();
            return Arrays.stream(trackPaging.getItems()).filter(t -> t.getAlbum() != null).findFirst().flatMap(z -> {
                String image = Arrays.stream(z.getAlbum().getImages()).findFirst().map(Image::getUrl).orElse(null);
                return Optional.of(new dao.entities.Album(-1, -1, z.getAlbum().getName(), image, null, null, z.getAlbum().getId()));
            });
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            Chuu.getLogger().info(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<dao.entities.Track> getTracklistFromId(String id, String artist) {
        ArrayList<dao.entities.Track> tracks = new ArrayList<>();
        if (id == null || id.isBlank()) {
            return tracks;
        }
        try {
            return Arrays.stream(spotifyApi.getAlbum(id).market(CountryCode.NZ).build().execute().getTracks().getItems()).map(x -> {
                dao.entities.Track track = new dao.entities.Track(artist, x.getName(), 0, false, x.getDurationMs() / 1000);
                track.setPosition(x.getTrackNumber() - 1);
                track.setSpotifyId(x.getId());
                return track;
            }).toList();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn("Error reading tracklist by id {}", id, e);
            return tracks;
        }
    }

    public List<AudioFeatures> getAudioFeatures(Set<String> ids) {
        initRequest();
        List<AudioFeatures> audioFeatures = new ArrayList<>();
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
                audioFeatures.addAll(Arrays.stream(execute).filter(Objects::nonNull).toList());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }
        }


        return audioFeatures;


    }

    public List<dao.entities.Track> getAlbumTrackList(String artist, String album) {
        String id = "";
        try {
            List<AlbumResult> albumResults = privateSearchAlbums(artist, album);
            for (AlbumResult item : albumResults) {
                if (item != null) {
                    id = item.id();
                    break;
                }
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return getTracklistFromId(id, artist);


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
        if (artist == null) {
            return "";
        }
        Artist[] artists = searchArtist(artist);

        if (artists != null) {
            for (Artist item : artists) {
                Image[] images = item.getImages();
                if (images.length != 0)
                    return images[0].getUrl();
            }
        }
        return "";
    }

    public Optional<UrlAndId> getUrlAndId(String artist) {
        Artist[] artists = searchArtist(artist);
        if (artists == null) {
            return Optional.empty();
        }
        for (Artist item : artists) {
            Image[] images = item.getImages();
            if (images.length != 0)
                return Optional.of(new UrlAndId(images[0].getUrl(), item.getId()));
        }
        return Optional.empty();
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

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

    public record AlbumResult(String id, String artist, String album, String uri, String cover) {

    }
}
