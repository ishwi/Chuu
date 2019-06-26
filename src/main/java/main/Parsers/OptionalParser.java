package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

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

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList(PREFIX + commandName + "***\n" +
				"\t*--" + optionalWord + "*\n\n ");
	}
}
