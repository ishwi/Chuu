package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.SecondsTimeFrameCount;
import DAO.Entities.TimeFrameEnum;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.TimerFrameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class TimeSpentCommand extends ConcurrentCommand {
	public TimeSpentCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new TimerFrameParser(dao, TimeFrameEnum.WEEK);
	}

	@Override
	List<String> getAliases() {
		return Collections.singletonList("minutes");
	}

	@Override
	String getDescription() {
		return "Minutes listened last week";
	}

	@Override
	String getName() {
		return "Wasted On Music";
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) {
		String[] message;
		message = parser.parse(e);
		if (message == null)
			return;

		String username = message[0];
		String timeframe = message[1];
		if (!timeframe.equals("7day") && !timeframe.equals("1month") && !timeframe.equals("3month")) {
			sendMessageQueue(e, "Only [w]eek,[m]onth and [q]uarter is supported at the moment , sorry :'(");
			return;
		}
		try {
			long userId = getDao().getDiscordIdFromLastfm(username, e.getGuild().getIdLong());
			String usableString = this.getUserString(userId, e, username);
			SecondsTimeFrameCount wastedOnMusic = lastFM.getMinutesWastedOnMusic(username, timeframe);

			sendMessageQueue(e, "**" + usableString + "** played " +
					wastedOnMusic.getMinutes() +
					" minutes of music, " + String
					.format("(%d:%02d ", wastedOnMusic.getHours(),
							wastedOnMusic.getRemainingMinutes()) +
					CommandUtil.singlePlural(wastedOnMusic.getHours(), "hour", "hours") +
					"), listening to " + wastedOnMusic.getCount() + " different tracks in the last " +
					wastedOnMusic.getTimeFrame().toString()
							.toLowerCase());
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex) {
			parser.sendError("Internal Service Error, try again later", e);
		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);
		}
	}
}
