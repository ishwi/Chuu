package main.commands;

import dao.DaoImplementation;
import main.exceptions.InstanceNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class TotalArtistNumberCommand extends ConcurrentCommand {
	public TotalArtistNumberCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao);
	}

	@Override
	public String getDescription() {
		return ("Artists count of user ");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("scrobbled");
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		String lastFmName = returned[0];
		long discordID = Long.parseLong(returned[1]);
		String username = getUserStringConsideringGuildOrNot(e, discordID, lastFmName);

		int plays = getDao().getUserArtistCount(lastFmName);
		sendMessageQueue(e, "**" + username + "** has scrobbled **" + plays + "** different artists");

	}

	@Override
	public String getName() {
		return "Artist count ";
	}
}
