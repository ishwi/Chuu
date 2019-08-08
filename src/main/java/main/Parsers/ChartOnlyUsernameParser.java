package main.Parsers;

import DAO.DaoImplementation;
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
