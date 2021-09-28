package dao.entities;

import java.io.Serial;
import java.io.Serializable;

public class TrackWithArtistId extends Track implements Serializable {
    @Serial
    private static final long serialVersionUID = 1231212331L;

    private long artistId;
    private int plays;
    private String albumMbid;
    private String artistMbid;
    private String mbid;

    private String album;
    private long albumId;
    private int utc;

    public TrackWithArtistId(String artist, String name, int plays, boolean isLoved, int duration, int utc) {
        super(artist, name, plays, isLoved, duration);
        this.utc = utc;
    }


    public TrackWithArtistId(Track other, int utc) {
        this(other.getArtist(), other.getName(), other.getPlays(), other.isLoved(), other.getDuration(), utc);
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public int getUtc() {
        return utc;
    }

    public void setUtc(int utc) {
        this.utc = utc;
    }

    public String getAlbumMbid() {
        return albumMbid;
    }

    public void setAlbumMbid(String albumMbid) {
        this.albumMbid = albumMbid;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }
}
