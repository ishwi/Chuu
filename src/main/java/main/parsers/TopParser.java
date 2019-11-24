package main.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import main.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TopParser extends DaoParser {
	public TopParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	protected void setUpOptionals() {
		opts.add(new OptionalEntity("--artist", "use artist instead of albums"));
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

		LastFMData username = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);

		return new String[]{username.getName()};
	}


	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username***\n" +
				"\tIf username is not specified defaults to authors account \n";

	}
}
