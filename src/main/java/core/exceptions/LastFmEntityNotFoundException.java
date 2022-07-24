package core.exceptions;

import core.apis.last.exceptions.AlbumException;
import core.apis.last.exceptions.ArtistException;
import core.apis.last.exceptions.ExceptionEntity;
import core.apis.last.exceptions.TrackException;
import core.translations.Messages;

import static core.translations.TranslationManager.m;

public class LastFmEntityNotFoundException extends LastFmException {
    private final transient ExceptionEntity exceptionCause;

    public LastFmEntityNotFoundException(ExceptionEntity cause) {
        super("");
        this.exceptionCause = cause;
    }

    public String toMessage() {
        if (exceptionCause instanceof ArtistException a) {
            return m(Messages.ERROR_LASTFM_ARTIST_NOT_FOUND, a.getArtist());
        }
        if (exceptionCause instanceof AlbumException alb) {
            return m(Messages.ERROR_LASTFM_ALBUM_NOT_FOUND, alb.getAlbum(), alb.getArtist());
        }
        if (exceptionCause instanceof TrackException tr) {
            return m(Messages.ERROR_LASTFM_TRACK_NOT_FOUND, tr.getSong(), tr.getArtist());
        }
        return m(Messages.ERROR_LASTFM_USER_NOT_FOUND, exceptionCause.getUserName());
    }

    @Override
    public String toString() {
        return "LastFmEntityNotFoundException{" +
               "exceptionCause=" + exceptionCause +
               '}';
    }
}
