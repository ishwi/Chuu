package dao.entities;

public class AlbumGenre {
    private final String artist;
    private final String album;
    private final String genre;

    public AlbumGenre(String artist, String album, String genre) {
        this.artist = artist;
        this.album = album;
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getGenre() {
        return genre;
    }
}
