package dao.entities;

public class ProfileEntity {
    private String username;
    private String discordName;
    private String crownArtist;
    private String uniqueArtist;
    private String uniqueUrl;
    private String crownUrl;
    private String lastfmUrl;
    private String discordUrl;
    private int scrobbles;
    private int albums;
    private int artist;
    private int crowns;
    private int uniques;
    private int obscurityScore;
    private String date;

    public ProfileEntity(String username, String discordName, String crownArtist, String uniqueArtist, String uniqueUrl, String crownUrl, String lastfmUrl, String discordUrl, int scrobbles, int albums, int artist, int crowns, int uniques, int obscurityScore, String date) {
        this.username = username;
        this.discordName = discordName;
        this.crownArtist = crownArtist;
        this.uniqueArtist = uniqueArtist;
        this.uniqueUrl = uniqueUrl;
        this.crownUrl = crownUrl;
        this.lastfmUrl = lastfmUrl;
        this.discordUrl = discordUrl;
        this.scrobbles = scrobbles;
        this.albums = albums;
        this.date = date;
        this.artist = artist;
        this.crowns = crowns;
        this.uniques = uniques;
        this.obscurityScore = obscurityScore;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLastfmUrl() {
        return lastfmUrl;
    }

    public void setLastfmUrl(String lastfmUrl) {
        this.lastfmUrl = lastfmUrl;
    }

    public String getDiscordUrl() {
        return discordUrl;
    }

    public void setDiscordUrl(String discordUrl) {
        this.discordUrl = discordUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public String getCrownArtist() {
        return crownArtist;
    }

    public void setCrownArtist(String crownArtist) {
        this.crownArtist = crownArtist;
    }

    public String getUniqueArtist() {
        return uniqueArtist;
    }

    public void setUniqueArtist(String uniqueArtist) {
        this.uniqueArtist = uniqueArtist;
    }

    public String getUniqueUrl() {
        return uniqueUrl;
    }

    public void setUniqueUrl(String uniqueUrl) {
        this.uniqueUrl = uniqueUrl;
    }

    public String getCrownUrl() {
        return crownUrl;
    }

    public void setCrownUrl(String crownUrl) {
        this.crownUrl = crownUrl;
    }

    public int getScrobbles() {
        return scrobbles;
    }

    public void setScrobbles(int scrobbles) {
        this.scrobbles = scrobbles;
    }

    public int getAlbums() {
        return albums;
    }

    public void setAlbums(int albums) {
        this.albums = albums;
    }

    public int getArtist() {
        return artist;
    }

    public void setArtist(int artist) {
        this.artist = artist;
    }

    public int getCrowns() {
        return crowns;
    }

    public void setCrowns(int crowns) {
        this.crowns = crowns;
    }

    public int getUniques() {
        return uniques;
    }

    public void setUniques(int uniques) {
        this.uniques = uniques;
    }

    public int getObscurityScore() {
        return obscurityScore;
    }

    public void setObscurityScore(int obscurityScore) {
        this.obscurityScore = obscurityScore;
    }
}
