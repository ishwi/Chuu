package core.apis.rym;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RYMSearch {

    public String searchUrl(String artist, String album) {
        album = album.replaceAll("(?i)( - )?(Single|EP)i", "");
        return "https://rateyourmusic.com/search?searchterm=%s%%20-%%20%s&searchtype=l".formatted(URLEncoder.encode(artist, StandardCharsets.UTF_8), URLEncoder.encode(album, StandardCharsets.UTF_8));
    }

    public String searchUrl(String artistAlbum) {
        artistAlbum = artistAlbum.replaceAll("(?i)( - )?(Single|EP)", "");
        return "https://rateyourmusic.com/search?searchterm=%s&searchtype=l".formatted(URLEncoder.encode(artistAlbum, StandardCharsets.UTF_8));
    }
}
