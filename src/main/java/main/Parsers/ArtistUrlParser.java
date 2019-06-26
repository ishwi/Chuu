package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ArtistUrlParser extends Parser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] message = getSubMessage(e.getMessage());


		boolean noUrl = true;

		String artist;
		String url = null;
		if (message.length >= 2) {
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
			sendError(getErrorMessage(1), e);

			return null;
		}
		return new String[]{artist, url};
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(0, "You need to specify the artist and the url!!");
		errorMessages.put(1, "You didnt specify a valid URL");
		errorMessages.put(2, "Something happened while processing the image");


	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList(PREFIX + commandName + "* *artist url***\n\n");

	}


}
