package dao.entities;

public class PresenceInfo {
    private final String artist;
    private final String url;
    private final long sum;
    private long discordId;

    public PresenceInfo(String artistName, String url, long summa, long discordID) {
        this.artist = artistName;
        this.url = url;
        this.sum = summa;
        this.discordId = discordID;
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return url;
    }

    public long getSum() {
        return sum;
    }

    public long getDiscordId() {
        return discordId;
    }
}
