package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class OneWordParser extends Parser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] subMessage = getSubMessage(e.getMessage());
		if (subMessage.length != 1) {
			sendError(getErrorMessage(0), e);
			return null;
		}
		return subMessage;
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(0, "You need to introduce a word!");
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *lastFmName***\n\n");

	}


}
