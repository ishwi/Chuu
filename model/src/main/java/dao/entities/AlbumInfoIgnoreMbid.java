package dao.entities;

import java.util.Objects;

public class AlbumInfoIgnoreMbid extends AlbumInfo {
    public AlbumInfoIgnoreMbid(String mbid) {
        super(mbid);
    }

    public AlbumInfoIgnoreMbid(String mbid, String name, String artist) {
        super(mbid, name, artist);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbumInfo)) return false;
        if (!super.equals(o)) return false;
        AlbumInfo albumInfo = (AlbumInfo) o;

        return ((getName().equals(albumInfo.getName())
                 || getName().equals("S/T")
                 || albumInfo.getName().equals("S/T")) && getArtist().equals(albumInfo.getArtist())) && (getMbid().equals(albumInfo.getMbid()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtist(), getName(), getMbid());
    }
}
