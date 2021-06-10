package dao.entities;

public class GlobalReturnNowPlayingSong extends GlobalReturnNowPlaying {

    private final String song;

    public GlobalReturnNowPlayingSong(GlobalReturnNowPlaying other, String song) {
        this(other.getDiscordId(), other.getLastFMId(), other.getArtist(), other.getPlayNumber(), other.getPrivacyMode(), song);
    }


    public GlobalReturnNowPlayingSong(long discordId, String lastFMId, String artist, int playNumber, PrivacyMode privacyMode, String song) {
        super(discordId, lastFMId, artist, playNumber, privacyMode);
        this.song = song;
    }

    public String getSong() {
        return song;
    }

}
