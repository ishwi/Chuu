package main.APIs.Parsers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UrlParser extends OneWordParser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		String[] submessage = super.parse(e);
		if (submessage == null)
			return null;
		String url = submessage[0];
		if (!isValidURL(url)) {
			sendError(getErrorMessage(1), e);
			return null;
		}
		return new String[]{url};


	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(1, "Invalid url ");
		errorMessages.put(2, "Insufficient Permissions, only a mod  can");

	}

}