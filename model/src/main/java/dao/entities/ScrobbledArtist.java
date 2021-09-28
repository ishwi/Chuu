package dao.entities;

import dao.utils.LinkUtils;

import java.util.Objects;

public class ScrobbledArtist {
    private long artistId = -1L;
    private String discordID;
    private String artist;
    private int count;
    private String artistMbid;
    private String url;
    private boolean updateBit = false;

    public ScrobbledArtist(String discordID, String artist, int count) {
        this.discordID = discordID;
        this.artist = artist;

        this.count = count;
    }

    public ScrobbledArtist(String artist, int count, String url) {
        this.artist = artist;
        this.count = count;
        this.url = url;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public boolean isUpdateBit() {
        return updateBit;
    }

    public void setUpdateBit(boolean updateBit) {
        this.updateBit = updateBit;
    }

    public String getDiscordID() {
        return discordID;
    }

    public void setDiscordID(String discordID) {
        this.discordID = discordID;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    @Override
    public int hashCode() {
        int result = discordID != null ? discordID.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + count;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (updateBit ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScrobbledArtist that = (ScrobbledArtist) o;

        if (count != that.count) return false;
        if (updateBit != that.updateBit) return false;
        if (!Objects.equals(discordID, that.discordID)) return false;
        if (!Objects.equals(artist, that.artist)) return false;
        return Objects.equals(url, that.url);
    }

    @Override
    public String toString() {

        return ". " +
               "[" +
               LinkUtils.cleanMarkdownCharacter(artist) +
               "](" + LinkUtils.getLastFmArtistUrl(artist) +
               ")" +
               " - " + count + " plays" +
               "\n";
    }
}
