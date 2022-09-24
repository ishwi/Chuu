package core.parsers.params;

import core.commands.Context;
import dao.entities.AlbumInfo;

import java.time.Year;

public class AlbumYearParameters extends CommandParameters {
    private final Year year;
    private final AlbumInfo albumInfo;

    public AlbumYearParameters(Context c, Year year, AlbumInfo albumInfo) {
        super(c);
        this.year = year;
        this.albumInfo = albumInfo;
    }


    public Year getYear() {
        return year;
    }

    public AlbumInfo getAlbumInfo() {
        return albumInfo;
    }
}
