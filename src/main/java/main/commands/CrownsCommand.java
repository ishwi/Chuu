package main.commands;

import dao.DaoImplementation;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;
import main.otherlisteners.Reactionary;
import main.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownsCommand extends ConcurrentCommand {
	public CrownsCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
		this.respondInPrivate = false;

	}

	@Override
	public String getDescription() {
		return ("List of artist you are the top listener from");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("crowns");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) {
		String[] returned = parser.parse(e);
		if (returned == null)
			return;
		String lastFmName = returned[0];
		//long discordID = Long.parseLong(returned[1]);

		UniqueWrapper<UniqueData> uniqueDataUniqueWrapper = getDao().getCrowns(lastFmName, e.getGuild().getIdLong());
		List<UniqueData> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();
		int rows = resultWrapper.size();
		if (rows == 0) {
			sendMessageQueue(e, "You don't have any crown :'(");
			return;
		}

		StringBuilder a = new StringBuilder();
		for (int i = 0; i < 10 && i < rows; i++) {
			UniqueData g = resultWrapper.get(i);
			a.append(i + 1).append(g.toString());
		}

		Member whoD = e.getGuild().getMemberById(uniqueDataUniqueWrapper.getDiscordId());
		String name = whoD == null ? lastFmName : whoD.getEffectiveName();

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription(a);
		embedBuilder.setColor(CommandUtil.randomColor());
		embedBuilder.setTitle(name + "'s crowns", CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
		embedBuilder.setFooter(name + " has " + resultWrapper.size() + " crowns!!\n", null);
		if (whoD != null)
			embedBuilder.setThumbnail(whoD.getUser().getAvatarUrl());

		MessageBuilder mes = new MessageBuilder();
		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
				new Reactionary<>(resultWrapper, message1, embedBuilder));
	}

	@Override
	public String getName() {
		return "Your own top";
	}


}



