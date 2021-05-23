package core.apis.last.entities;

import core.commands.utils.CommandUtil;

public record Scrobble(String artist, String album, String song, String image, Long duration) {
    public String toLines() {
        String s = album == null || album.isBlank() ? "" : "\nAlbum: **" + album + "**";
        return String
                .format("Artist: **%s**\nSong: **%s**%s", artist, song, s);
    }

    public String toLink(String uri) {
        return "[%s](%s)".formatted(lineless(), CommandUtil.escapeMarkdown(uri));
    }

    public String lineless() {
        return "%s - %s%s".formatted(song, artist, album != null ? " | " + album : "");
    }

    public String linelessReversed() {
        return "%s - %s%s".formatted(artist, song, album != null ? " | " + album : "");
    }

}
