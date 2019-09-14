package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UniqueData;
import DAO.Entities.UniqueWrapper;
import main.OtherListeners.Reactionary;
import main.Parsers.OnlyUsernameParser;
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
	public List<String> getAliases() {
		return Collections.singletonList("crowns");
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
	public void onCommand(MessageReceivedEvent e) {
		String[] message;

		message = parser.parse(e);
		if (message == null)
			return;

		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		StringBuilder a = new StringBuilder();
		UniqueWrapper<UniqueData> uniqueDataUniqueWrapper = getDao().getCrowns(message[0], e.getGuild().getIdLong());
		List<UniqueData> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();
		int rows = resultWrapper.size();
		if (resultWrapper.isEmpty()) {
			sendMessageQueue(e, "You don't have any crown :'(");
			return;
		}
		for (int i = 0; i < 10 && i < rows;i++) {
			UniqueData g = resultWrapper.get(i);
			a.append(i + 1).append(g.toString());
		}

		embedBuilder.setDescription(a);
		embedBuilder.setColor(CommandUtil.randomColor());
		Member whoD = e.getGuild().getMemberById(uniqueDataUniqueWrapper.getDiscordId());
		String name = whoD == null ? message[0] : whoD.getEffectiveName();
		embedBuilder.setTitle(name + "'s crowns", CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
		embedBuilder.setFooter(name + " has " + resultWrapper.size() + " crowns!!\n", null);
		if (whoD != null)
			embedBuilder.setThumbnail(whoD.getUser().getAvatarUrl());

		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
				new Reactionary<>(resultWrapper, message1, embedBuilder));
	}


}



