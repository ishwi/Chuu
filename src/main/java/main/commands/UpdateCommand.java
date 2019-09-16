package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UpdateCommand extends MyCommandDbAccess {
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
	public void onCommand(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);

		if (returned == null)
			return;
		String lastFmName = returned[0];
		long discordID = Long.parseLong(returned[1]);
		String userString = getUserStringConsideringGuildOrNot(e, discordID, lastFmName);

		try {
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


		} catch (LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmEntityNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(4), e);

		} catch (LastFmException ex) {
			sendMessageQueue(e, "Error happened while updating " + userString + "'s account  , sorry uwu");
		}


	}

	@Override
	public String getName() {
		return "Update";
	}


}
