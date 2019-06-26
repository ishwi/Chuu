package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class TopParser extends DaoParser {
	public TopParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] subMessage = getSubMessage(e.getMessage());
		String[] optional = containsOptional("artist", subMessage);
		boolean hasArtist = (subMessage.length != optional.length);
		if (!hasArtist) {
			subMessage = optional;
		}
		String username = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		if (username == null)
			return null;
		return new String[]{username, Boolean.toString(hasArtist)};
	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(3, "Not a valid lastFm username!");
		errorMessages.put(2, "Internal Server Error");
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList(PREFIX + commandName + "* *username***\n" +
				"\tIf username is not specified defaults to authors account \n\n");

	}
}
