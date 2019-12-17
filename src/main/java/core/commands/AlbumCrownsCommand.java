package core.commands;

import dao.DaoImplementation;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
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
	public String getDescription() {
		return ("List of albums you are the top listener from");
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("crownsalbum", "crownsal");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned = parser.parse(e);
		String lastFmName = returned[0];
		long discordID = Long.parseLong(returned[1]);

		Member whoD = e.getGuild().getMemberById(discordID);
		String name = whoD != null ? whoD
				.getEffectiveName() : getUserStringConsideringGuildOrNot(e, discordID, lastFmName);


		UniqueWrapper<UniqueData> uniqueDataUniqueWrapper = getDao()
				.getUserAlbumCrowns(lastFmName, e.getGuild().getIdLong());
		List<UniqueData> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

		int rows = resultWrapper.size();
		if (rows == 0) {
			sendMessageQueue(e, name + " doesn't have any album crown :'(");
			return;
		}

		StringBuilder a = new StringBuilder();
		for (int i = 0; i < 10 && i < rows; i++) {
			UniqueData g = resultWrapper.get(i);
			a.append(i + 1).append(g.toString());
		}
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription(a);
		embedBuilder.setColor(CommandUtil.randomColor());

		embedBuilder
				.setTitle(name + "'s album crowns", CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
		embedBuilder.setFooter(name + " has " + resultWrapper.size() + " album crowns!!\n", null);
		if (whoD != null)
			embedBuilder.setThumbnail(whoD.getUser().getAvatarUrl());

		MessageBuilder mes = new MessageBuilder();
		e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
				new Reactionary<>(resultWrapper, message1, embedBuilder));
	}

	@Override
	public String getName() {
		return "Your own album top";
	}

}



