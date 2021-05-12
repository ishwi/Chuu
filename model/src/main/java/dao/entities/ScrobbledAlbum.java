package dao.entities;

import java.util.Objects;

public class ScrobbledAlbum extends ScrobbledArtist {
    private long albumId = -1L;
    private String album;
    private String albumMbid;

    public ScrobbledAlbum(String discordID, String artist, int count, long albumId, String album, String albumMbid) {
        super(discordID, artist, count);
        this.albumId = albumId;
        this.album = album;
        this.albumMbid = albumMbid;
    }

    public ScrobbledAlbum(Album album, String artistName) {
        super(artistName, 0, album.url());
        this.albumId = album.id();
        setArtistId(album.artistId());
        this.album = album.albumName();
    }

    public ScrobbledAlbum(long albumId, String url) {
        super(null, 0, url);
        this.albumId = albumId;
    }

    public ScrobbledAlbum(String artist, int count, String url, long albumId, String album, String albumMbid) {
        super(artist, count, url);
        this.albumId = albumId;
        this.album = album;
        this.albumMbid = albumMbid;
    }

    public ScrobbledAlbum(String albumName, String artistName, String url, String albumMbid) {
        super(artistName, 0, url);
        this.album = albumName;
        this.albumMbid = albumMbid;
    }

    public String getAlbumMbid() {
        return albumMbid;
    }

    public void setAlbumMbid(String albumMbid) {
        this.albumMbid = albumMbid;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    @Override
    public String toString() {
        return "ScrobbledAlbum{" +
               "albumId=" + albumId +
               ", album='" + album + '\'' +
               ", albumMbid='" + albumMbid + '\'' +
               '}';
    }

    @Override
    public int hashCode() {
        int result = getDiscordID() != null ? getDiscordID().hashCode() : 0;
        result = 31 * result + (getArtist() != null ? getArtist().hashCode() : 0);
        result = 31 * result + getCount();
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + (getAlbum() != null ? getAlbum().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(album, ((ScrobbledAlbum) o).album);
    }
}


