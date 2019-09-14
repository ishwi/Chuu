package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import main.Parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UpdateCommand extends MyCommandDbAccess {
	public UpdateCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("update");
	}

	@Override
	public String getDescription() {
		return "Keeps you up to date ";
	}

	@Override
	public String getName() {
		return "Update";
	}

	@Override
	public void onCommand(MessageReceivedEvent e) {
		String[] message;
		MessageBuilder mes = new MessageBuilder();

		message = parser.parse(e);

		if (message == null)
			return;

		try {
			if (getDao().getAll(e.getGuild().getIdLong()).stream()
					.noneMatch(s -> s.getLastFMName().equals(message[0]))) {
				sendMessage(e, message[0] + " is not registered in this guild");
				return;
			}
			List<ArtistData> list = lastFM.getLibrary(message[0]);
			getDao().insertArtistDataList(list, message[0]);
			mes.setContent("Successfully updated " + message[0] + " info !").sendTo(e.getChannel()).queue();


		} catch (LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3), e);

		} catch (LastFmException ex) {
			sendMessage(e, "Error happened while updating , sorry uwu");
		}


	}


}
