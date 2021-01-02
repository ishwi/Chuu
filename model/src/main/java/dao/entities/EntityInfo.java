package dao.entities;

import java.util.Objects;

public class EntityInfo {
    private String mbid;
    private String artist;

    EntityInfo(String mbid, String artist) {
        this.mbid = mbid;
        this.artist = artist;
    }

    public EntityInfo(String mbid) {
        this.mbid = mbid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityInfo)) return false;
        EntityInfo that = (EntityInfo) o;
        if (getArtist() == null) {
            return that.getArtist() == null;
        }
        return ((getArtist().isBlank() && that.getArtist().isBlank()) || (Objects.equals(getArtist() == null ? getArtist() : getArtist().toLowerCase(), that.getArtist() == null ? that.getArtist() : that.getArtist().toLowerCase()))) || ((getMbid() != null) && !getMbid().isBlank() && (that.getMbid() != null) && !that.getMbid().isBlank() && Objects.equals(getMbid(), that.getMbid()));
    }

    @Override
    public int hashCode() {
        if (getMbid() != null && !getMbid().isBlank()) {
            return Objects.hash(getMbid());
        }
        return Objects.hash(getArtist().toLowerCase());
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }
}
