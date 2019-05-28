package main.APIs.Parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WhoKnowsParser extends Parser {
	public WhoKnowsParser(MessageReceivedEvent e) {
		super(e);
	}

	@Override
	public String[] parse() {

		String[] message = getSubMessage(e.getMessage());
		boolean hasImage = false;
		String[] optional = containsOptional("image", message);
		if (optional.length == message.length) {
			hasImage = true;
			message = optional;
		}

		if (message.length == 0) {
			sendError(getErrorMessage(1));
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
