package main.Exceptions;

class ParseException extends Exception {
	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
