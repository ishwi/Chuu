package dao.entities;

import java.time.Year;
import java.util.List;

public class MusicbrainzFullAlbumEntity extends FullAlbumEntityExtended {
    private final List<String> tags;
    private final Year year;


    public MusicbrainzFullAlbumEntity(FullAlbumEntityExtended fullAlbumEntity, List<String> tags, Year year) {
        super(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum(), fullAlbumEntity.getTotalPlayNumber(), fullAlbumEntity.getAlbumUrl(), fullAlbumEntity.getUsername(), fullAlbumEntity.getListeners(), fullAlbumEntity.getTotalscrobbles());
        this.tags = tags;
        this.year = year;
    }

    public List<String> getTags() {
        return tags;
    }

    public Year getYear() {
        return year;
    }
}
