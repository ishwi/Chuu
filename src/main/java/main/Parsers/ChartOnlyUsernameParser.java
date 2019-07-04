package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ChartOnlyUsernameParser extends OnlyUsernameParser {
	public ChartOnlyUsernameParser(DaoImplementation dao) {
		super(dao);
	}


	@Override
	public String[] parse(MessageReceivedEvent e) {

		String[] message = getSubMessage(e.getMessage());

		FlagParser flagParser = new FlagParser(message);
		boolean writeTitles = !flagParser.contains("notitles");
		boolean writePlays = flagParser.contains("plays");
		message = flagParser.getMessage();

		String discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (discordName == null) {
			return null;
		}
		return new String[]{discordName, Boolean.toString(writeTitles), Boolean.toString(writePlays)};
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *username*** \n" +
				"\t If username is not specified defaults to authors account\n" +
				"\tcan use --notitles to not display titles\n" +
				"\tcan use --plays to display plays\n\n");

	}
}
