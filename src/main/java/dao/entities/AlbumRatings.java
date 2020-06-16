package dao.entities;

import core.commands.CommandUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Year;
import java.util.List;

public class AlbumRatings {
    private final long albumId;
    private final long artistId;
    private final String artistName;
    private final String albumName;
    private final List<Rating> userRatings;
    private final Year releaseYear;

    public AlbumRatings(long albumId, long artistId, String artistName, String albumName, List<Rating> userRatings, Year releaseYear) {
        this.albumId = albumId;
        this.artistId = artistId;
        this.artistName = artistName;
        this.albumName = albumName;
        this.userRatings = userRatings;
        this.releaseYear = releaseYear;
    }

    public long getAlbumId() {
        return albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public List<Rating> getUserRatings() {
        return userRatings;
    }

    public Year getReleaseYear() {
        return releaseYear;
    }


}
