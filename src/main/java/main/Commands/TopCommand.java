package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.LastFmUserNotFoundException;
import main.Exceptions.ParseException;
import main.last.ConcurrentLastFM;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class TopCommand extends ConcurrentCommand {
	public TopCommand(DaoImplementation dao) {
		super(dao);
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!top");
	}

	@Override
	public String getDescription() {
		return ("Your all time top albums!");
	}

	@Override
	public String getName() {
		return "Top Albums Chart";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList
				("**!top username**\n" + "\tIf username is not specified defaults to authors account \n\n");

	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		return new String[]{getLastFmUsername1input(getSubMessage(e.getMessage()), e.getAuthor().getIdLong(), e)};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:";
		if (code == 0) {
			userNotOnDB(e, code);
			return;
		} else if (code == 3) {
			sendMessage(e, base + cause + " is not a real lastFM username");
			return;
		}
		sendMessage(e, base + "There was a problem with Last FM Api" + cause);
	}

	@Override
	public void threadableCode() {
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
		embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
				.setDescription("Most Listened Artists");
		mes.setEmbed(embed.build());
		try {
			e.getChannel().sendFile(ConcurrentLastFM.getUserList(message[0], "overall", 5, 5), "cat.png", mes.build()).queue();
		} catch (LastFMServiceException ex) {
			errorMessage(e, 1, ex.getMessage());
		} catch (LastFmUserNotFoundException e1) {
			errorMessage(e, 3, e1.getMessage());
		}

	}
}
