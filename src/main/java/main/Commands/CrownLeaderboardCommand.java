package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.CrownsLbEntry;
import main.OtherListeners.Reactionary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CrownLeaderboardCommand extends ConcurrentCommand {

	public CrownLeaderboardCommand(DaoImplementation dao) {
		super(dao);
		this.respondInPrivate = false;

	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		List<CrownsLbEntry> crownList = getDao().getGuildCrownLb(e.getGuild().getIdLong());
		crownList.forEach(cl -> cl.setDiscordName(getUserString(cl.getDiscordId(), e, cl.getLastFmId())));
		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor()).setThumbnail(e.getGuild().getIconUrl());
		StringBuilder a = new StringBuilder();


		if (crownList.size() == 0) {
			sendMessage(e, "This guild has no registered users:(");
			return;
		}

		for (int i = 0; i < 10 && i < crownList.size(); i++) {
			a.append(i + 1).append(crownList.get(i).toString());
		}
		embedBuilder.setDescription(a).setTitle(e.getGuild().getName() + "'s Crowns leadearboard")
				.setThumbnail(e.getGuild().getIconUrl()).setFooter(e.getGuild().getName() + " has " + crownList.size() + " registered users!\n", null);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(message ->
				new Reactionary<>(crownList, message, embedBuilder)
		);
	}


	@Override
	public List<String> getAliases() {
		return Arrays.asList("!crownslb", "!lb", "!leaderboard");
	}

	@Override
	public String getDescription() {
		return ("Crowns per user ordered desc");
	}

	@Override
	public String getName() {
		return "Crowns Leaderboard";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!lb \n");
	}


}
