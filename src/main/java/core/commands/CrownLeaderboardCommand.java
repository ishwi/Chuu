package core.commands;

import dao.DaoImplementation;
import dao.entities.LbEntry;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownLeaderboardCommand extends ConcurrentCommand {
	String entryName = "Crowns";

	public CrownLeaderboardCommand(DaoImplementation dao) {
		super(dao);
		this.respondInPrivate = false;
		this.parser = new NoOpParser();

	}

	@Override
	public String getDescription() {
		return ("Crowns per user ordered desc");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("crownslb");
	}

	@Override
	public String getName() {
		return "Crowns Leaderboard";
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		List<LbEntry> list = getList(e.getGuild().getIdLong());
		printList(list, e);
	}

	List<LbEntry> getList(long guildId) {
		return getDao().getGuildCrownLb(guildId);
	}

	private void printList(List<LbEntry> list, MessageReceivedEvent e) {
		list.forEach(cl -> cl.setDiscordName(getUserString(cl.getDiscordId(), e, cl.getLastFmId())));
		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
				.setThumbnail(e.getGuild().getIconUrl());
		StringBuilder a = new StringBuilder();

		if (list.isEmpty()) {
			sendMessageQueue(e, "This guild has no registered users:(");
			return;
		}

		for (int i = 0; i < 10 && i < list.size(); i++) {
			a.append(i + 1).append(list.get(i).toString());
		}
		embedBuilder.setDescription(a).setTitle(e.getGuild().getName() + "'s " + entryName + " leadearboard")
				.setThumbnail(e.getGuild().getIconUrl())
				.setFooter(e.getGuild().getName() + " has " + list.size() + " registered users!\n", null);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(message ->
				new Reactionary<>(list, message, embedBuilder)
		);
	}


}
