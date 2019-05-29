package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OneWordParser extends Parser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] submessage = getSubMessage(e.getMessage());
		if (submessage.length != 1) {
			sendError(getErrorMessage(0), e);
			return null;
		}
		return submessage;
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(0, "You need to introduce a word!");
	}
}
