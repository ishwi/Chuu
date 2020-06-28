package dao.entities;

public class TrackWithArtistId extends Track {
    private long artistId;
    private int plays;
    private String album;
    private long albumId;

    public TrackWithArtistId(String artist, String name, int plays, boolean isLoved, int duration) {
        super(artist, name, plays, isLoved, duration);
    }


    public TrackWithArtistId(Track other) {
        this(other.getArtist(), other.getName(), other.getPlays(), other.isLoved(), other.getDuration());
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    @Override
    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
}
