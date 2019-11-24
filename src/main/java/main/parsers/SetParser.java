package main.parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetParser extends Parser {
	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(0, "You need to introduce only a valid last.fm account!");
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
