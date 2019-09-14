package main.parsers;

import dao.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OnlyUsernameParser extends DaoParser {
	public OnlyUsernameParser(DaoImplementation dao) {
		super(dao);
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {

		String discordName = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		if (discordName == null) {
			return null;
		}
		return new String[]{discordName};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username*** \n" +
				"\t If username is not specified defaults to authors account\n";
	}
}
