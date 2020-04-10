package dao.entities;

public class AlbumUserPlays {
    private String album;
    private String albumUrl;
    private int plays;
    private String artist;

    public AlbumUserPlays(String album, String albumUrl) {
        this.album = album;
        this.albumUrl = albumUrl;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }
}
