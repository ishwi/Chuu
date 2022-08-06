package dao.entities;

public class ReturnNowPlayingSong extends ReturnNowPlaying {
    private final String song;

    public ReturnNowPlayingSong(ReturnNowPlaying other, String song) {
        this(other.getDiscordId(), other.getLastFMId(), other.getArtist(), other.getPlayNumber(), song);
    }

    public ReturnNowPlayingSong(long discordId, String lastFMId, String artist, long playNumber, String song) {
        super(discordId, lastFMId, artist, playNumber);
        this.song = song;
    }

    public String getSong() {
        return song;
    }
}
