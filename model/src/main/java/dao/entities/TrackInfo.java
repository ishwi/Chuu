package dao.entities;

import java.util.Objects;

public class TrackInfo extends EntityInfo {
    private String album;
    private String track;
    private String albumMid;


    public TrackInfo(String artist, String album, String track, String albumMid) {
        super(null, artist);
        this.album = album;
        this.track = track;
        this.albumMid = albumMid;
    }


    public TrackInfo(String mbid, String albumMid) {
        super(mbid);
        this.albumMid = albumMid;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackInfo trackInfo)) return false;
        if (!super.equals(o)) return false;
        if (getMbid() != null && !getMbid().isBlank() && trackInfo.getMbid() != null && !trackInfo.getMbid().isBlank()) {
            return super.equals(o);
        }
        return Objects.equals(getTrack(), trackInfo.getTrack()) && Objects.equals(getArtist(), trackInfo.getArtist());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtist(), getTrack());
    }

    public String getAlbumMid() {
        return albumMid;
    }

    public void setAlbumMid(String albumMid) {
        this.albumMid = albumMid;
    }
}
