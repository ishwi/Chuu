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
        return Objects.equals(getArtist(), that.getArtist()) || (getMbid() != null && !getMbid().isBlank() && that.getMbid() != null && !that.getMbid().isBlank() && Objects.equals(getMbid(), that.getMbid()));
    }

    @Override
    public int hashCode() {
        if (getMbid() != null && !getMbid().isBlank()) {
            return Objects.hash(getMbid());
        }
        return Objects.hash(getArtist());
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
