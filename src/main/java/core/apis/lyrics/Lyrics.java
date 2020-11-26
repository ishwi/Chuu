package core.apis.lyrics;

import javax.annotation.Nullable;

public class Lyrics {
    private final String lyrics;
    private final @Nullable String songName;
    private final @Nullable String artistName;
    private final @Nullable String imageUrl;

    public Lyrics(String lyrics, String songName, String artistName) {
        this(lyrics, songName, artistName, null);
    }

    public Lyrics(String lyrics, @Nullable String songName, @Nullable String artistName, @Nullable String imageUrl) {
        this.lyrics = lyrics;
        this.songName = songName;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
    }

    public String getLyrics() {
        return lyrics;
    }

    public @Nullable String getSongName() {
        return songName;
    }

    public @Nullable String getArtistName() {
        return artistName;
    }

    public @Nullable String getImageUrl() {
        return imageUrl;
    }
}
