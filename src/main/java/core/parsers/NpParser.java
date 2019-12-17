package core.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import core.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NpParser extends DaoParser {
	public NpParser(DaoImplementation dao) {
		super(dao);
	}

	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
		LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		return new String[]{data.getName(), String.valueOf(data.getDiscordId())};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *username***\n" +
				"\t If username is not specified defaults to authors account\n";
	}
}
