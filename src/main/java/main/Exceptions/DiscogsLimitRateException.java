package main.Exceptions;

public class DiscogsLimitRateException extends Exception {
	public DiscogsLimitRateException(String message) {
		super(message);
	}
}
