package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.ParseException;
import net.dv8tion.jda.core.EmbedBuilder;
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
		EmbedBuilder embed = new EmbedBuilder();
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


		} catch (LastFMServiceException | LastFMNoPlaysException ex) {
			errorMessage(e, 1, ex.getMessage());
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
		userNotOnDB(e, code);

	}
}
