package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UniqueData;
import DAO.Entities.UniqueWrapper;
import main.OtherListeners.Reactionario;
import main.Parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.List;

public class CrownsCommand extends ConcurrentCommand {
	public CrownsCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] message;

		message = parser.parse(e);
		if (message == null)
			return;

		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		StringBuilder a = new StringBuilder();
		UniqueWrapper<UniqueData> uniqueDataUniqueWrapper = getDao().getCrowns(message[0], e.getGuild().getIdLong());
		List<UniqueData> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();
		if (resultWrapper.isEmpty()) {
			sendMessage(e, "You are a puny mortal without crowns");
			return;
		}
		for (int i = 0; i < 10; i++) {
			UniqueData g = resultWrapper.get(i);
			a.append(i + 1).append(g.toString());
		}

		embedBuilder.setDescription(a);
		embedBuilder.setColor(CommandUtil.randomColor());
		Member whoD = e.getGuild().getMemberById(uniqueDataUniqueWrapper.getDiscordId());
		String name = whoD == null ? message[0] : whoD.getEffectiveName();
		embedBuilder.setTitle(name + "'s crowns");
		embedBuilder.setFooter(name + " has " + resultWrapper.size() + " crowns!!\n", null);
		if (whoD != null)
			embedBuilder.setThumbnail(whoD.getUser().getAvatarUrl());

		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(quee -> {
			quee.addReaction("U+2B05").submit();
			quee.addReaction("U+27A1").submit();
			ListenerAdapter adapter = new Reactionario<>(resultWrapper, quee, embedBuilder);
			e.getJDA().addEventListener(adapter);
			try {
				Thread.sleep(40000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			e.getJDA().removeEventListener(adapter);
			quee.clearReactions().queue();
		});

	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!crowns");
	}

	@Override
	public String getDescription() {
		return ("List of artist you are the top listener from");
	}

	@Override
	public String getName() {
		return "Your own top";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!crowns **user \n" +
				"If user is missing defaults to user account\n\n ");
	}


}



