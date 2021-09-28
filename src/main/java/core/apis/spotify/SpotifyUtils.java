package core.apis.spotify;

public class SpotifyUtils {

    private static final String playlist = "https://open.spotify.com/playlist/%s";
    private static final String album = "https://open.spotify.com/album/%s";


    public static String getPlaylistLink(String identifier) {
        return playlist.formatted(identifier);
    }

    public static String getAlbumLink(String identifier) {
        return album.formatted(identifier);
    }
}
