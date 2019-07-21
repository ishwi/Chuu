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

public class UniqueCommand extends ConcurrentCommand {
	public UniqueCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
		this.respondInPrivate = false;

	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] message;
		message = parser.parse(e);
		if (message == null)
			return;

		String lastFmId = message[0];
		UniqueWrapper<UniqueData> resultWrapper = getDao().getUniqueArtist(e.getGuild().getIdLong(), lastFmId);

		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
				.setThumbnail(e.getGuild().getIconUrl());
		StringBuilder a = new StringBuilder();

		int rows = resultWrapper.getUniqueData().size();

		if (rows == 0) {
			sendMessage(e, "You have no Unique Artists :(");
			return;
		}
		String lastFMID = resultWrapper.getLastFmId();

		for (int i = 0; i < 10 && i < rows; i++) {
			UniqueData g = resultWrapper.getUniqueData().get(i);
			a.append(i + 1).append(g.toString());
		}
		Member member = e.getGuild().getMemberById(resultWrapper.getDiscordId());
		assert (member != null);
		embedBuilder.setDescription(a).setTitle(member
				.getEffectiveName() + "'s Top 10 unique Artists", "https://www.last.fm/user/" + lastFMID)
				.setThumbnail(member.getUser().getAvatarUrl())
				.setFooter(member.getEffectiveName() + " has " + rows + " unique artists!\n", null);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(m ->
				new Reactionary<>(resultWrapper.getUniqueData(), m, embedBuilder)
		);

	}

	@Override
	public String getDescription() {
		return ("Returns lists of all the unique artist you have scrobbled");
	}

	@Override
	public String getName() {
		return "Unique List Of Artists";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!unique");
	}


}
