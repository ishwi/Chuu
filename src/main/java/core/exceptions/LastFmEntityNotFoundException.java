package core.exceptions;

import core.apis.last.exceptions.AlbumException;
import core.apis.last.exceptions.ArtistException;
import core.apis.last.exceptions.ExceptionEntity;
import core.apis.last.exceptions.TrackException;
import core.commands.CommandUtil;

public class LastFmEntityNotFoundException extends LastFmException {
    private final transient ExceptionEntity exceptionCause;

    public LastFmEntityNotFoundException(ExceptionEntity cause) {
        super("");
        this.exceptionCause = cause;
    }

    public String toMessage() {
        if (exceptionCause instanceof ArtistException) {
            return String.format("The artist %s doesn't exist on last.fm", CommandUtil.cleanMarkdownCharacter(((ArtistException) exceptionCause).getArtist()));
        }
        if (exceptionCause instanceof AlbumException) {
            AlbumException mew = (AlbumException) this.exceptionCause;
            return String.format("The album %s doesn't exist on last.fm", CommandUtil.cleanMarkdownCharacter(mew.getAlbum() + " by " + mew.getArtist()));
        }
        if (exceptionCause instanceof TrackException) {
            TrackException mew = (TrackException) this.exceptionCause;
            return String.format("The song %s doesn't exist on last.fm", CommandUtil.cleanMarkdownCharacter(mew.getSong() + " by " + mew.getArtist()));
        } else
            return "The user " + CommandUtil.cleanMarkdownCharacter(exceptionCause.getUserName()) + " doesn't exist on last.fm";
    }

    @Override
    public String toString() {
        return "LastFmEntityNotFoundException{" +
                "exceptionCause=" + exceptionCause +
                '}';
    }
}
