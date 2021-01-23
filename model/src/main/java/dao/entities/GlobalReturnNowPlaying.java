package dao.entities;

import java.util.function.Consumer;

public class GlobalReturnNowPlaying extends ReturnNowPlaying {
    private final PrivacyMode privacyMode;
    private Consumer<GlobalReturnNowPlaying> globalDisplayer;

    public GlobalReturnNowPlaying(long discordId, String lastFMId, String artist, int playNumber, PrivacyMode privacyMode) {
        super(discordId, lastFMId, artist, playNumber);
        this.privacyMode = privacyMode;
    }


    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }
}


