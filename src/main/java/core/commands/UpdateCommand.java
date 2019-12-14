package core.commands;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UpdateCommand extends MyCommand {
	public UpdateCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
	}

	@Override
	public String getDescription() {
		return "Keeps you up to date ";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("update");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);

		if (returned == null)
			return;
		String lastFmName = returned[0];
		long discordID = Long.parseLong(returned[1]);
		String userString = getUserStringConsideringGuildOrNot(e, discordID, lastFmName);

			if (e.isFromGuild()) {
				if (getDao().getAll(e.getGuild().getIdLong()).stream()
						.noneMatch(s -> s.getLastFMName().equals(lastFmName))) {
					sendMessageQueue(e, userString + " is not registered in this guild");
					return;
				}
			} else if (!getDao().getMapGuildUsers().containsValue(e.getAuthor().getIdLong())) {
				sendMessageQueue(e, "You are not registered yet, go to any server and register there!");
				return;
			}

			List<ArtistData> list = lastFM.getLibrary(lastFmName);
			getDao().insertArtistDataList(list, lastFmName);
			sendMessageQueue(e, "Successfully updated " + userString + " info !");




	}

	@Override
	public String getName() {
		return "Update";
	}


}
