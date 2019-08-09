package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NpParser extends DaoParser {
	public NpParser(DaoImplementation dao) {
		super(dao);
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		String username = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		if (username == null)
			return null;
		return new String[]{username};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username***\n" +
				"\t If username is not specified defaults to authors account\n";
	}
}
