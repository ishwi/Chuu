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

import java.util.Arrays;
import java.util.List;

public class AlbumCrownsCommand extends ConcurrentCommand {
	public AlbumCrownsCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
		this.respondInPrivate = false;
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("crownsalbum", "crownsal");
	}

	@Override
	public String getDescription() {
		return ("List of albums you are the top listener from");
	}

	@Override
	public String getName() {
		return "Your own album top";
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
		UniqueWrapper<UniqueData> uniqueDataUniqueWrapper = getDao()
				.getUserAlbumCrowns(message[0], e.getGuild().getIdLong());
		List<UniqueData> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();
		int rows = resultWrapper.size();
		if (resultWrapper.isEmpty()) {
			sendMessageQueue(e, "You don't have any album crown :'(");
			return;
		}
		for (int i = 0; i < 10 && i < rows; i++) {
			UniqueData g = resultWrapper.get(i);
			a.append(i + 1).append(g.toString());
		}

		embedBuilder.setDescription(a);
		embedBuilder.setColor(CommandUtil.randomColor());
		Member whoD = e.getGuild().getMemberById(uniqueDataUniqueWrapper.getDiscordId());
		String name = whoD == null ? message[0] : whoD.getEffectiveName();
		embedBuilder
				.setTitle(name + "'s album crowns", CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
		embedBuilder.setFooter(name + " has " + resultWrapper.size() + " album crowns!!\n", null);
		if (whoD != null)
			embedBuilder.setThumbnail(whoD.getUser().getAvatarUrl());

		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
				new Reactionary<>(resultWrapper, message1, embedBuilder));
	}

}



