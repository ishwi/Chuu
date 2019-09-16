package main.commands;

import dao.DaoImplementation;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import main.otherlisteners.Reactionary;
import main.parsers.TimerFrameParser;
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
	String getDescription() {
		return "Top songs in the provided period";
	}

	@Override
	List<String> getAliases() {
		return Arrays.asList("toptracks", "tt");
	}

	@Override
	void onCommand(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;
		String username = returned[0];
		long discordId = Long.parseLong(returned[1]);
		String timeframe = returned[2];
		try {

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
			embedBuilder.setThumbnail(url.toString());
			e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build())
					.queue(message -> new Reactionary<>(listTopTrack, message, embedBuilder));
		} catch (LastFMNoPlaysException ex) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}
	}

	@Override
	String getName() {
		return "Top tracks";
	}
}
