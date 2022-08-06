package dao.entities;

public class GlobalReturnNowPlaying extends ReturnNowPlaying {
    private final PrivacyMode privacyMode;

    public GlobalReturnNowPlaying(long discordId, String lastFMId, String artist, long playNumber, PrivacyMode privacyMode) {
        super(discordId, lastFMId, artist, playNumber);
        this.privacyMode = privacyMode;
    }


    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }
}


