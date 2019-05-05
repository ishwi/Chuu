package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UpdateCommand extends MyCommandDbAccess {
	public UpdateCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String[] message;
		MessageBuilder mes = new MessageBuilder();
		try {
			message = parse(e);
		} catch (ParseException e1) {

			errorMessage(e, 0, e1.getMessage());
			return;
		}


		try {
			if (getDao().getAll(e.getGuild().getIdLong()).stream().noneMatch(s -> s.getLastFMName().equals(message[0]))) {
				sendMessage(e, message[0] + " is not registered in this guild");
				return;
			}
			LinkedList<ArtistData> list = lastFM.getLibrary(message[0]);
			getDao().updateUserLibrary(list, message[0]);
			mes.setContent("Sucessfully updated " + message[0] + " info !").sendTo(e.getChannel()).queue();


		} catch (LastFMNoPlaysException e1) {
			errorMessage(e, 3, e1.getMessage());

		} catch (LastFmException ex) {
			errorMessage(e, 2, ex.getMessage());
		}


	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!update");
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
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!update *user\n\tIf user is missing defaults to user account\n\n");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		return new String[]{getLastFmUsername1input(getSubMessage(e.getMessage()), e.getAuthor().getIdLong(), e)};

	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		switch (code) {
			case 2:
				message = "There was a problem with Last FM Api" + cause;
				break;
			case 3:
				message = "User hasnt played any song recently!";
				break;
			default:
				userNotOnDB(e, code);
				return;

		}
		sendMessage(e, base + message);
	}
}
