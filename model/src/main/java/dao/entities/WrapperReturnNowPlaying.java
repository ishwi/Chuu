package dao.entities;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WrapperReturnNowPlaying {
    private List<ReturnNowPlaying> returnNowPlayings;
    private int rows;
    private String url;
    private @NotNull String artist;

    public WrapperReturnNowPlaying(List<ReturnNowPlaying> returnNowPlayings, int rows, String url, @NotNull String artist) {
        this.returnNowPlayings = returnNowPlayings;
        this.rows = rows;
        this.url = url;
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public @NotNull String getArtist() {
        return artist;
    }

    public void setArtist(@NotNull String artist) {
        this.artist = artist;
    }

    public List<ReturnNowPlaying> getReturnNowPlayings() {
        return returnNowPlayings;
    }

    public void setReturnNowPlayings(List<ReturnNowPlaying> returnNowPlayings) {
        this.returnNowPlayings = returnNowPlayings;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }


}
