package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OneWordParser extends Parser {
	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(0, "You need to introduce a word!");
	}

	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		if (subMessage.length != 1) {
			sendError(getErrorMessage(0), e);
			return null;
		}
		return subMessage;
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *lastFmName***\n";

	}


}
