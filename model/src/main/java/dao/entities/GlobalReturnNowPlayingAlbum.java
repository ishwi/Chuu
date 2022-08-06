package dao.entities;

public class GlobalReturnNowPlayingAlbum extends GlobalReturnNowPlaying {

    private final String album;


    public GlobalReturnNowPlayingAlbum(long discordId, String lastFMId, String artist, long playNumber, PrivacyMode privacyMode, String album) {
        super(discordId, lastFMId, artist, playNumber, privacyMode);
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

}
