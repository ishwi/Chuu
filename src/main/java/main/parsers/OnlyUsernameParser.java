package main.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class OnlyUsernameParser extends DaoParser {
	public OnlyUsernameParser(DaoImplementation dao) {
		super(dao);
	}

	public OnlyUsernameParser(DaoImplementation dao, OptionalEntity... strings) {
		super(dao);
		opts.addAll(Arrays.asList(strings));
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {

		LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		if (data == null) {
			return null;
		}
		return new String[]{data.getName(), String.valueOf(data.getDiscordId())};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username*** \n" +
				"\t If username is not specified defaults to authors account\n";
	}
}
