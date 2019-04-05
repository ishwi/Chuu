package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import main.Exceptions.ParseException;
import main.last.ConcurrentLastFM;
import main.Exceptions.LastFMServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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


		e.getChannel().sendTyping().queue();
		try {

			LinkedList<ArtistData> list = ConcurrentLastFM.getLibrary(message[0]);
			getDao().addData(list, message[0]);
			mes.setContent("Sucessfully updated " + message[0] + " info !").sendTo(e.getChannel()).queue();


		} catch (LastFMServiceException ex) {
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
		return Collections.singletonList("**!update lastFmUser**\\n\\n");
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
