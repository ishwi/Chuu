package dao.entities;

import java.util.Objects;

public class AlbumInfo extends EntityInfo {
    private String name;

    public AlbumInfo(String name, String artist) {
        super(null, artist);
        this.name = name;
    }

    public AlbumInfo(String mbid, String name, String artist) {
        super(mbid, artist);
        this.name = name;
    }

    public AlbumInfo(String mbid) {
        super(mbid, null);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbumInfo albumInfo)) return false;
        if (getMbid() != null && !getMbid().isBlank() && albumInfo.getMbid() != null && !albumInfo.getMbid().isBlank()) {
            return super.equals(o);
        }
        if (!super.equals(o)) return false;
        return (getName().equalsIgnoreCase(albumInfo.getName())
                || getName().equalsIgnoreCase("S/T")
                || albumInfo.getName().equalsIgnoreCase("S/T")) && getArtist().equalsIgnoreCase(albumInfo.getArtist());
    }

    @Override
    public int hashCode() {
        if (getMbid() != null && !getMbid().isBlank()) {
            return Objects.hash(getMbid());
        }
        return Objects.hash(getArtist().toLowerCase(), getName().toLowerCase());
    }
}
