package dao.entities;

public class GlobalReturnNowPlayingSong extends GlobalReturnNowPlaying {

    private final String song;


    public GlobalReturnNowPlayingSong(long discordId, String lastFMId, String artist, long playNumber, PrivacyMode privacyMode, String song) {
        super(discordId, lastFMId, artist, playNumber, privacyMode);
        this.song = song;
    }

    public String getSong() {
        return song;
    }

}
