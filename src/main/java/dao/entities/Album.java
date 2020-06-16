package dao.entities;

import java.util.UUID;

public class Album {
    private final long id;
    private final long artist_id;
    private final String album_name;
    private final String url;
    private final Long rym_id;
    private final UUID mbid;
    private final String spotify_id;

    public Album(long id, long artist_id, String album_name, String url, Long rym_id, UUID mbid, String spotify_id) {
        this.id = id;
        this.artist_id = artist_id;
        this.album_name = album_name;
        this.url = url;
        this.rym_id = rym_id;
        this.mbid = mbid;
        this.spotify_id = spotify_id;
    }

    public long getId() {
        return id;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public String getUrl() {
        return url;
    }

    public Long getRym_id() {
        return rym_id;
    }

    public UUID getMbid() {
        return mbid;
    }

    public String getSpotify_id() {
        return spotify_id;
    }
}
