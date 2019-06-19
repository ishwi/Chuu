package main.Exceptions;

class DiscogsLimitRateException extends Exception {
	public DiscogsLimitRateException(String message) {
		super(message);
	}
}
