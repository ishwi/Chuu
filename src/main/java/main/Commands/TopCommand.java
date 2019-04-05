package main.Commands;

import DAO.DaoImplementation;
import main.last.ConcurrentLastFM;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

public class TopCommand extends MyCommandDbAccess {
	public TopCommand(DaoImplementation dao) {
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
			//Unrechable
			return;
		}
		e.getChannel().sendTyping().queue();
		embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
				.setDescription("Most Listened Artists");
		mes.setEmbed(embed.build());
		try {


			e.getChannel().sendFile(ConcurrentLastFM.getUserList(message[0], "overall", 5, 5), "cat.png", mes.build()).queue();
		} catch (LastFMServiceException ex) {
			onLastFMError(e);
		}

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
}
