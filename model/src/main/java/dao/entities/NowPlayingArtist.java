package dao.entities;

import java.util.Objects;

public class NowPlayingArtist {
    private String artistName;
    private String artistMbid;
    private String albumMbid;
    private boolean nowPlaying;
    private String albumName;
    private String songName;
    private String url;
    private String username;

    public NowPlayingArtist(String artistName, String artistMbid, boolean nowPlaying, String albumName, String songName, String url, String username) {
        this.artistName = artistName;
        this.artistMbid = artistMbid;
        this.nowPlaying = nowPlaying;
        this.albumName = albumName;
        this.songName = songName;
        this.url = url;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    public boolean isNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(boolean nowPlaying) {
        this.nowPlaying = nowPlaying;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        int result = artistName != null ? artistName.hashCode() : 0;
        result = 31 * result + (artistMbid != null ? artistMbid.hashCode() : 0);
        result = 31 * result + (nowPlaying ? 1 : 0);
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (songName != null ? songName.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NowPlayingArtist that = (NowPlayingArtist) o;

        if (nowPlaying != that.nowPlaying) return false;
        if (!Objects.equals(artistName, that.artistName)) return false;
        if (!Objects.equals(artistMbid, that.artistMbid)) return false;
        if (!Objects.equals(albumName, that.albumName)) return false;
        if (!Objects.equals(songName, that.songName)) return false;
        return Objects.equals(url, that.url);
    }

    public String getAlbumMbid() {
        return albumMbid;
    }

    public void setAlbumMbid(String albumMbid) {
        this.albumMbid = albumMbid;
    }
}