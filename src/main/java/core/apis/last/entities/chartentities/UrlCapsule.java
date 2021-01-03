package core.apis.last.entities.chartentities;

import core.imagerenderer.ChartLine;

import java.util.List;
import java.util.Objects;

public abstract class UrlCapsule {

    private String artistName;
    private String albumName;
    private int pos;
    private String url;
    private int plays;
    private String mbid;
    private boolean neverendingMode = false;

    public UrlCapsule(String url, int pos, String albumName, String artistName, String mbid, int plays) {
        this.url = url;
        this.pos = pos;
        this.artistName = artistName;
        this.albumName = albumName;
        this.mbid = mbid;
        this.plays = plays;
    }

    public UrlCapsule(String url, int pos, String artistName, String albumName, String mbid) {
        this.url = url;
        this.pos = pos;
        this.artistName = artistName;
        this.albumName = albumName;
        this.mbid = mbid;
    }

    public abstract List<ChartLine> getLines();

    public abstract String toEmbedDisplay();

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    @Override
    public String toString() {
        return this.toEmbedDisplay();
    }

    public abstract String toChartString();

    public abstract int getChartValue();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlCapsule)) return false;
        UrlCapsule that = (UrlCapsule) o;
        return
                Objects.equals(getArtistName(), that.getArtistName()) &&
                        Objects.equals(getAlbumName(), that.getAlbumName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtistName(), getAlbumName());
    }

    public boolean isNeverendingMode() {
        return neverendingMode;
    }

    public void setNeverendingMode(boolean neverendingMode) {
        this.neverendingMode = neverendingMode;
    }
}
