package main.APIs.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WhoKnowsParser extends OptionalParser {
	public WhoKnowsParser() {
		super("list");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] message = getSubMessage(e.getMessage());

		String[] optional = super.parse(e);
		boolean hasImage = true;
		if (optional.length != message.length) {
			hasImage = false;
			message = optional;
		}

		if (message.length == 0) {
			sendError(getErrorMessage(1), e);
			return null;
			//No Commands Inputed
			//throw new ParseException("Input");
		}
		String artist = artistMultipleWords(message);

		return new String[]{artist, Boolean.toString(hasImage)};

	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(1, "No artist specified!");
	}
}
