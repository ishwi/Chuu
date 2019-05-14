package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UniqueData;
import DAO.Entities.UniqueWrapper;
import main.Exceptions.ParseException;
import main.OtherListeners.Reactionario;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class UniqueCommand extends MyCommandDbAccess {
	public UniqueCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String[] message;
		try {
			message = parse(e);
		} catch (ParseException e1) {
			if (e1.getMessage().equals("Commands")) {
				errorMessage(e, 0, e1.getMessage());
				return;
			}
			if (e1.getMessage().equals("DB")) {
				errorMessage(e, 1, e1.getMessage());
				return;
			}
			errorMessage(e, 100, e1.getMessage());
			return;
		}
		String lastFmId = message[0];
		UniqueWrapper<UniqueData> resultWrapper = getDao().getUniqueArtist(e.getGuild().getIdLong(), lastFmId);

		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(e.getGuild().getIconUrl());
		StringBuilder a = new StringBuilder();


		int rows = resultWrapper.getRows();

		if (rows == 0) {
			sendMessage(e, "You have no Unique Artists :(");
			return;
		}
		String lastFMID = resultWrapper.getLastFmId();

		for (int i = 0; i < 10; i++) {
			UniqueData g = resultWrapper.getUniqueData().get(i);
			a.append(i + 1).append(g.toString());
		}
		Member member = e.getGuild().getMemberById(resultWrapper.getDiscordId());

		embedBuilder.setDescription(a).setTitle(member.getEffectiveName() + "'s Top 10 unique Artists", "https://www.last.fm/user/" + lastFMID)
				.setThumbnail(member.getUser().getAvatarUrl()).setFooter(member.getEffectiveName() + " has " + rows + " unique artists!\n", null);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(quee -> {
			quee.addReaction("U+2B05").submit();
			quee.addReaction("U+27A1").submit();
			ListenerAdapter adapter = new Reactionario<>(resultWrapper.getUniqueData(), quee, embedBuilder);
			e.getJDA().addEventListener(adapter);
			try {
				Thread.sleep(40000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			e.getJDA().removeEventListener(adapter);
			quee.clearReactions().queue();
		});
		return;

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!unique");
	}

	@Override
	public String getDescription() {
		return ("Returns lists of all the unique artits you have scrobbled");
	}

	@Override
	public String getName() {
		return "Unique List Of Artists";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!unique *user\n\tIf user is missing defaults to user account\n\n ");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String[] subMessage = getSubMessage(e.getMessage());
		if (subMessage.length == 0) {
			try {
				return new String[]{getDao().findShow(e.getAuthor().getIdLong()).getName()};
			} catch (InstanceNotFoundException e1) {
				throw new ParseException("DB");

			}
		}
		if (subMessage.length != 1) {
			throw new ParseException("Commands");
		}
		return new String[]{getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e)
		};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;
		switch (code) {
			case 0:
				message = "You need to introduce an user";
				break;
			case 1:
				message = "User was not found on the database, register first!";
				break;
			default:
				message = "An unknown Error happened";

		}
		sendMessage(e, base + message);
	}
}
