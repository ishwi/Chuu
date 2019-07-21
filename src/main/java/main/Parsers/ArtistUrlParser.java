package main.Parsers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ArtistUrlParser extends Parser {
	@Override
	public void setUpErrorMessages() {
		errorMessages.put(0, "You need to specify the artist and the url !!");
		errorMessages.put(1, "You didnt specify a valid URL");
		errorMessages.put(2, "Couldn't get an Image from link supplied");


	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] message = getSubMessage(e.getMessage());

		boolean noUrl = true;

		String artist;
		String url = null;
		if (message.length >= 1) {
			StringBuilder a = new StringBuilder();
			for (String s : message) {
				if (noUrl && isValidURL(s)) {
					noUrl = false;
					url = s;
					continue;
				}
				a.append(s).append(" ");
			}
			artist = a.toString().trim();

		} else {
			sendError(getErrorMessage(0), e);
			return null;
		}
		if (url == null) {
			if (e.getMessage().getAttachments().isEmpty()) {
				sendError(getErrorMessage(1), e);
				return null;

			} else {
				Message.Attachment attachment = e.getMessage().getAttachments().get(0);
				url = attachment.getUrl();
			}
		}
		return new String[]{artist, url};
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *artist url***\n\n");

	}


}
