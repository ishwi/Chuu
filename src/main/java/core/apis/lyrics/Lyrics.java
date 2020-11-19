package core.apis.lyrics;

public class Lyrics {
    private final String lyrics;
    private final String songName;
    private final String artistName;
    private final String imageUrl;

    public Lyrics(String lyrics, String songName, String artistName) {
        this(lyrics, songName, artistName, null);
    }

    public Lyrics(String lyrics, String songName, String artistName, String imageUrl) {
        this.lyrics = lyrics;
        this.songName = songName;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
