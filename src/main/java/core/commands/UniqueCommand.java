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

import java.util.Collections;
import java.util.List;

public class UniqueCommand extends ConcurrentCommand {
	public UniqueCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
		this.respondInPrivate = false;

	}

	@Override
	public String getDescription() {
		return ("Returns lists of all the unique artist you have scrobbled");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("unique");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned = parser.parse(e);
		String lastFmName = returned[0];
		//long discordID = Long.parseLong(returned[1]);
		UniqueWrapper<UniqueData> resultWrapper = getDao().getUniqueArtist(e.getGuild().getIdLong(), lastFmName);


		int rows = resultWrapper.getUniqueData().size();
		if (rows == 0) {
			sendMessageQueue(e, "You have no Unique Artists :(");
			return;
		}

		StringBuilder a = new StringBuilder();
		for (int i = 0; i < 10 && i < rows; i++) {
			UniqueData g = resultWrapper.getUniqueData().get(i);
			a.append(i + 1).append(g.toString());
		}
		Member member = e.getGuild().getMemberById(resultWrapper.getDiscordId());
		assert (member != null);

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
				.setThumbnail(e.getGuild().getIconUrl());
		embedBuilder.setDescription(a).setTitle(member
				.getEffectiveName() + "'s Top 10 unique Artists", CommandUtil.getLastFmUser(lastFmName))
				.setThumbnail(member.getUser().getAvatarUrl())
				.setFooter(member.getEffectiveName() + " has " + rows + " unique artists!\n", null);

		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(m ->
				new Reactionary<>(resultWrapper.getUniqueData(), m, embedBuilder)
		);

	}

	@Override
	public String getName() {
		return "Unique List Of Artists";
	}


}
