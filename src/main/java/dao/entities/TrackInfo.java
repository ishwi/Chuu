package dao.entities;

import java.util.Objects;

public class TrackInfo extends EntityInfo {
    private String album;
    private String track;


    public TrackInfo(String artist, String album, String track) {
        super(null, artist);

        this.album = album;
        this.track = track;
    }


    public TrackInfo(String mbid) {
        super(mbid);
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
        if (!(o instanceof TrackInfo)) return false;
        if (!super.equals(o)) return false;
        TrackInfo trackInfo = (TrackInfo) o;
        if (getMbid() != null && !getMbid().isBlank() && trackInfo.getMbid() != null && !trackInfo.getMbid().isBlank()) {
            return super.equals(o);
        }
        return Objects.equals(getTrack(), trackInfo.getTrack()) && Objects.equals(getArtist(), trackInfo.getArtist());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtist(), getTrack());
    }
}
