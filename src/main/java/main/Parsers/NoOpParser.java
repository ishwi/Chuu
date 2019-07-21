package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class NoOpParser extends Parser {
	@Override
	protected void setUpErrorMessages() {
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + "**\n\n");
	}
}
