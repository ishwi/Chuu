package main.parsers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URL;

public class UrlParser extends Parser {
	@Override
	public void setUpErrorMessages() {
		errorMessages.put(1, "Invalid url ");
		errorMessages.put(2, "Insufficient Permissions, only a mod  can");

	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		String url;

		if (subMessage == null || subMessage.length == 0) {
			if (e.getMessage().getAttachments().isEmpty()) {
				return new String[]{};
			} else {
				url = e.getMessage().getAttachments().get(0).getUrl();
			}
		} else if (subMessage.length == 1) {
			url = subMessage[0];
			if (!isValidURL(url)) {
				sendError(getErrorMessage(1), e);
				return null;
			}

		} else {
			sendError("You need to give only a url or an attachment", e);
			return null;
		}
		return new String[]{url};


	}

	static boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *url***\n" +
				"\t User needs to have administration permissions\n";
	}
}