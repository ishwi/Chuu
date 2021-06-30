package dao.entities;

import java.util.regex.Pattern;

public enum RandomTarget {
    SPOTIFY(Pattern
            .compile("^(https://open.spotify.com/(album|artist|track|playlist)/|spotify:(album|artist|track|playlist):)([a-zA-Z0-9]{22})(?:\\?.*)?$"), "https://open.spotify.com/"),

    YOUTUBE(Pattern
            .compile("(?:https?://)?(?:(?:www\\.)?|music\\.)?youtu\\.?be(?:\\.com)?/?.*(?:watch|embed)?(?:.*v=|v/|/)([\\w-_]{11}).*?$"), "https://www.youtube.com/watch?v="),
    DEEZER(Pattern.compile("^https?://(?:www\\.)?deezer\\.com/(?:\\w+/)?(track|album|playlist)/(\\d+).*$"), "https://www.deezer.com"),
    SOUNDCLOUD(Pattern
            .compile("((https://)|(http://)|(www.)|(m\\.)|(\\s))+(soundcloud.com/)+[a-zA-Z0-9\\-.]+(/)+[a-zA-Z0-9\\-.]+(/)?+[a-zA-Z0-9\\-.]+?"), "soundcloud.com/"),
    BANDCAMP(Pattern
            .compile("(http://(.*\\.bandcamp\\.com/|.*\\.bandcamp\\.com/track/.*|.*\\.bandcamp\\.com/album/.*))|(https://(.*\\.bandcamp\\.com/|.*\\.bandcamp\\.com/track/.*|.*\\.bandcamp\\.com/album/.*))"), "bandcamp.com");

    public final Pattern regex;
    public final String contains;

    RandomTarget(Pattern regex, String contains) {
        this.regex = regex;
        this.contains = contains;
    }
}
