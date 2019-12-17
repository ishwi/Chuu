package core.exceptions;

import core.apis.last.ExceptionEntity;

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
			return "The user " + exceptionCause.getUserName() + " doesn't exist on last.fm";
		}
		if (exceptionCause.getAlbumName() == null) {
			return "The artist " + exceptionCause.getArtistName() + " doesn't exist on last.fm";
		} else return "The entity " + exceptionCause.getArtistName() + " - " + exceptionCause
				.getAlbumName() + " doesn't exist on last.fm";

	}

	@Override
	public String toString() {
		return "LastFmEntityNotFoundException{" +
				"exceptionCause=" + exceptionCause +
				'}';
	}
}
