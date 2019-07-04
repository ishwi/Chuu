package main.Parsers;

import java.util.Arrays;

public class FlagParser {
	private String[] message;

	public FlagParser(String[] message) {
		this.message = message;
	}

	public boolean contains(String optional) {
		int ogLength = message.length;
		message = Arrays.stream(message).filter(s -> !s.equals("--" + optional)).toArray(String[]::new);
		return (ogLength != message.length);
	}

	public String[] getMessage() {
		return message;
	}
}
