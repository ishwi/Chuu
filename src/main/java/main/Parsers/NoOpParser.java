package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class NoOpParser extends Parser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void setUpErrorMessages() {
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList(PREFIX + commandName + "***\n\n");
	}
}
