package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.TimerFrameParser;
import dao.DaoImplementation;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class UserTopTrackCommand extends ConcurrentCommand {
	public UserTopTrackCommand(DaoImplementation dao) {
		super(dao);
		parser = new TimerFrameParser(dao, TimeFrameEnum.WEEK);
		respondInPrivate = false;
	}

	@Override
	public String getDescription() {
		return "Top songs in the provided period";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("toptracks", "tt");
	}

	@Override
	public String getName() {
		return "Top tracks";
	}

	@Override
	void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);
		String username = returned[0];
		long discordId = Long.parseLong(returned[1]);
		String timeframe = returned[2];

		List<Track> listTopTrack = lastFM.getListTopTrack(username, timeframe);
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 10 && i < listTopTrack.size(); i++) {
			Track g = listTopTrack.get(i);
			s.append(i + 1).append(g.toString());
		}

		StringBuilder url = new StringBuilder();
		StringBuilder usableName = new StringBuilder();

		CommandUtil.getUserInfoConsideringGuildOrNot(usableName, url, e, discordId);

		MessageBuilder messageBuilder = new MessageBuilder();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription(s);
		embedBuilder.setColor(CommandUtil.randomColor());

		embedBuilder
				.setTitle(usableName + "'s top  tracks in " + TimeFrameEnum.fromCompletePeriod(timeframe)
						.toString(), CommandUtil
						.getLastFmUser(timeframe));
		embedBuilder.setThumbnail(url.toString().isEmpty() ? null : url.toString());
		e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build())
				.queue(message ->
						executor.submit(() -> new Reactionary<>(listTopTrack, message, embedBuilder)));

	}
}
