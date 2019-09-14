package main.commands;

import dao.DaoImplementation;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.Track;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DailyCommand extends ConcurrentCommand {
	public DailyCommand(DaoImplementation dao) {
		super(dao);
		parser = new OnlyUsernameParser(dao);
	}

	@Override
	String getDescription() {
		return "Return time spent listening in the last 24 hours";
	}

	@Override
	List<String> getAliases() {
		return Collections.singletonList("daily");
	}

	@Override
	void onCommand(MessageReceivedEvent e) {
		String[] parse = parser.parse(e);
		if (parse == null)
			return;
		String lastfmName = parse[0];
		try {
			Map<Track, Integer> durationsFromWeek = lastFM.getDurationsFromWeek(lastfmName);
			SecondsTimeFrameCount minutesWastedOnMusicDaily = lastFM
					.getMinutesWastedOnMusicDaily(lastfmName, durationsFromWeek, (int) Instant.now()
							.minus(1, ChronoUnit.DAYS).getEpochSecond());
			long userId = getDao().getDiscordIdFromLastfm(lastfmName, e.getGuild().getIdLong());
			String usableString = this.getUserStringConsideringGuildOrNot(e, userId, lastfmName);
			sendMessageQueue(e, "**" + usableString + "** played " +
					minutesWastedOnMusicDaily.getMinutes() +
					" minutes of music, " + String
					.format("(%d:%02d ", minutesWastedOnMusicDaily.getHours(),
							minutesWastedOnMusicDaily.getRemainingMinutes()) +
					CommandUtil.singlePlural(minutesWastedOnMusicDaily.getHours(), "hour", "hours") +
					"), listening to " + minutesWastedOnMusicDaily
					.getCount() + " tracks in the last 24 hours");

		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex) {
			parser.sendError("Internal Service Error, try again later", e);
		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);


		}
	}

	@Override
	String getName() {
		return "Daily";
	}
}
