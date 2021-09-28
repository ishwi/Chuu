package dao.entities;

import java.util.List;

public record ArtistSummary(int userPlayCount, int listeners, int playcount, List<String> similars,
                            List<String> tags, String summary,
                            String artistname, String mbid) {

}
