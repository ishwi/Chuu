package dao.entities;

import java.util.UUID;

public record Album(long id, long artistId, String albumName, String url, Long rymId,
                    UUID mbid, String spotifyId) {


}
