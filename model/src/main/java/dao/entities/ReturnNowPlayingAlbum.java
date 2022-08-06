package dao.entities;

public class ReturnNowPlayingAlbum extends ReturnNowPlaying {
    private final String album;

    public ReturnNowPlayingAlbum(ReturnNowPlaying other, String album) {
        this(other.getDiscordId(), other.getLastFMId(), other.getArtist(), other.getPlayNumber(), album);
    }

    public ReturnNowPlayingAlbum(long discordId, String lastFMId, String artist, long playNumber, String album) {
        super(discordId, lastFMId, artist, playNumber);
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }
}
