package core.translations;

public enum Messages {
    PREFIX_COMMAND_SUCCESS("prefix.success"),
    ERROR_LASTFM_ARTIST_NOT_FOUND("error.lastfm.artist.not-found"),
    ERROR_LASTFM_ALBUM_NOT_FOUND("error.lastfm.album.not-found"),
    ERROR_LASTFM_TRACK_NOT_FOUND("error.lastfm.track.not-found"),
    ERROR_LASTFM_USER_NOT_FOUND("error.lastfm.user.not-found");


    private final String key;

    Messages(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
