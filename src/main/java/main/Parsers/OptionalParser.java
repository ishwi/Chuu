package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OptionalParser extends Parser {
	private final String optionalWord;

	public OptionalParser(String optionalWord) {
		this.optionalWord = optionalWord;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] message = getSubMessage(e.getMessage());
		return containsOptional(optionalWord, message);
	}

	@Override
	public void setUpErrorMessages() {

	}
}
