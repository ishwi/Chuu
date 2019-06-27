package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class NpParser extends DaoParser {
	public NpParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String[] message = getSubMessage(e.getMessage());
		String username = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (username == null)
			return null;
		return new String[]{username};
	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(2, "Internal Server Error, try again later");


	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *username***\n" +
				"\t If username is not specified defaults to authors account\n\n");
	}
}
