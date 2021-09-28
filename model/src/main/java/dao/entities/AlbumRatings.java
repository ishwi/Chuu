package dao.entities;

import java.time.Year;
import java.util.List;

public record AlbumRatings(long albumId, long artistId, String artistName, String albumName,
                           List<Rating> userRatings, Year releaseYear) {

}
