package dao.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class LinkUtils {

    public static final Pattern markdownStripper = Pattern.compile("((?<!\\\\)[*_~|`>\\[()\\]])");

    public static String getLastFmArtistUrl(String artist) {
        return "https://www.last.fm/music/" + encodeUrl(artist);
    }

    public static String getLastFmArtistAlbumUrl(String artist, String album) {
        return "https://www.last.fm/music/" + encodeUrl(artist) + "/" + encodeUrl(album);
    }

    public static String getLastFmTagUrl(String tag) {
        return "https://www.last.fm/tag/" + encodeUrl(tag);
    }

    public static String getMusicbrainzTagUrl(String tag) {
        return "https://musicbrainz.org/tag/" + encodeUrl(tag);
    }


    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String cleanMarkdownCharacter(String string) {
        return markdownStripper.matcher(string).replaceAll("\\\\$1");
    }

    public static String getLastFMArtistTrack(String artist, String track) {
        return getLastFmArtistUrl(artist) + "/_/" + encodeUrl(track);

    }

    public static String markdownLessString(String string) {
        if (!string.contains("\\")) {
            return string;
        }
        return string.replaceAll("\\\\", "");

    }

}
