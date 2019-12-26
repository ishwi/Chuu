package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import dao.DaoImplementation;
import dao.entities.UniqueData;
import dao.entities.UniqueWrapper;
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

	public boolean isGlobal() {
		return false;
	}

	public UniqueWrapper<UniqueData> getList(long guildId, String lastFmName) {
		return getDao().getCrowns(lastFmName, guildId);
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
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned = parser.parse(e);
		String lastFmName = returned[0];
		//long discordID = Long.parseLong(returned[1]);
		UniqueWrapper<UniqueData> uniqueDataUniqueWrapper = getList(e.getGuild().getIdLong(), lastFmName);
		List<UniqueData> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();
		int rows = resultWrapper.size();
		if (rows == 0) {
			sendMessageQueue(e, "You don't have any" + (isGlobal() ? " global " : " ") + "crown :'(");
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
		embedBuilder.setTitle(name + "'s " + (isGlobal() ? "global " : "") + "crowns", CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
		embedBuilder.setFooter(name + " has " + resultWrapper.size() + (isGlobal() ? " global" : "") + " crowns!!\n", null);
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



