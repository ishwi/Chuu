package main.Parsers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UrlParser extends OneWordParser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		String[] subMessage = super.parse(e);
		if (subMessage == null)
			return null;
		String url = subMessage[0];
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

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *url***\n" +
				"\t User needs to have administration permissions\n\n");
	}
}