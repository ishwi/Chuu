package core.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import core.exceptions.InstanceNotFoundException;
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

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {

		LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		return new String[]{data.getName(), String.valueOf(data.getDiscordId())};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username*** \n" +
				"\t If username is not specified defaults to authors account\n";
	}
}
