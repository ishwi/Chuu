package dao.entities;

public class GlobalReturnNowPlayingAlbum extends GlobalReturnNowPlaying {

    private final String album;

    public GlobalReturnNowPlayingAlbum(GlobalReturnNowPlaying other, String album) {
        this(other.getDiscordId(), other.getLastFMId(), other.getArtist(), other.getPlayNumber(), other.getPrivacyMode(), album);
    }


    public GlobalReturnNowPlayingAlbum(long discordId, String lastFMId, String artist, int playNumber, PrivacyMode privacyMode, String album) {
        super(discordId, lastFMId, artist, playNumber, privacyMode);
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

}
