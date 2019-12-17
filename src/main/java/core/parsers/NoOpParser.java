package core.parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NoOpParser extends Parser {


	@Override
	protected void setUpErrorMessages() {
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + "**\n\n";
	}
}
