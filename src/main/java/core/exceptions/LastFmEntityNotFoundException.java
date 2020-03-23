package core.exceptions;

import core.apis.last.ExceptionEntity;
import core.commands.CommandUtil;

public class LastFmEntityNotFoundException extends LastFmException {
    private ExceptionEntity exceptionCause;

    public LastFmEntityNotFoundException(ExceptionEntity cause) {
        super("");
        this.exceptionCause = cause;
    }

    public ExceptionEntity getExceptionCause() {
        return exceptionCause;
    }

    public void setExceptionCause(ExceptionEntity exceptionCause) {
        this.exceptionCause = exceptionCause;
    }

    public String toMessage() {
        if (exceptionCause.getArtistName() == null) {
            return "The user " + CommandUtil.cleanMarkdownCharacter(exceptionCause.getUserName()) + " doesn't exist on last.fm";
        }
        if (exceptionCause.getAlbumName() == null) {
            return "The artist " + CommandUtil.cleanMarkdownCharacter(exceptionCause.getArtistName()) + " doesn't exist on last.fm";
        } else
            return "The entity " + CommandUtil.cleanMarkdownCharacter(exceptionCause.getArtistName()) + " - " + CommandUtil.cleanMarkdownCharacter(exceptionCause
                    .getAlbumName()) + " doesn't exist on last.fm";

    }

    @Override
    public String toString() {
        return "LastFmEntityNotFoundException{" +
               "exceptionCause=" + exceptionCause +
               '}';
    }
}
