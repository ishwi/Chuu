package main.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import main.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartOnlyUsernameParser extends OnlyUsernameParser {
	public ChartOnlyUsernameParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	protected void setUpOptionals() {
		opts.add(new OptionalEntity("--notitles", "dont display titles"));
		opts.add((new OptionalEntity("--plays", "display play count")));

	}


	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

		LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		return new String[]{data.getName()};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username*** \n" +
				"\t If username is not specified defaults to authors account\n";

	}
}
