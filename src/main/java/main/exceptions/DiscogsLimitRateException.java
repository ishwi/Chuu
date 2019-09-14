package main.exceptions;

class DiscogsLimitRateException extends Exception {
	public DiscogsLimitRateException(String message) {
		super(message);
	}
}
