package main.Exceptions;

public abstract class LastFmException extends Exception {
	LastFmException(String message) {
		super(message);
	}
}
