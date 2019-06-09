package main.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistAlbumParser extends Parser {
	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] submessage = getSubMessage(e.getMessage());
		StringBuilder builder = new StringBuilder();

		if (submessage.length == 0) {
			return new String[]{Boolean.toString(true)};
		}

		for (String s : submessage) {
			builder.append(s).append(" ");
		}
		String s = builder.toString();
		String[] content = s.split("\\s*-\\s*");

		if (content.length != 2) {
			sendError(this.getErrorMessage(1), e);
			return null;
		}

		String artist = content[0].trim();
		String album = content[1].trim();

		return new String[]{Boolean.toString(false), artist, album};
	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(1, "You need to use - to separate");
		errorMessages.put(2, "Internal Server Error, try again later");
		errorMessages.put(3, "Didn't found what you were looking for");
	}
}
