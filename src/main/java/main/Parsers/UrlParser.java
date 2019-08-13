package main.Parsers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UrlParser extends OneWordParser {
	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(1, "Invalid url ");
		errorMessages.put(2, "Insufficient Permissions, only a mod  can");

	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		subMessage = super.parseLogic(e, subMessage);
		if (subMessage == null || subMessage.length == 0)
			return new String[]{};
		String url = subMessage[0];
		if (!isValidURL(url)) {
			sendError(getErrorMessage(1), e);
			return null;
		}
		return new String[]{url};


	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *url***\n" +
				"\t User needs to have administration permissions\n";
	}
}