package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.SecondsTimeFrameCount;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class TimeSpentCommand extends ConcurrentCommand {
	public TimeSpentCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao);
	}

	@Override
	protected void threadableCode(MessageReceivedEvent e) {
		String[] message;
		message = parser.parse(e);
		if (message == null)
			return;

		String username = message[0];
		try {
			long userId = getDao().getDiscordIdFromLastfm(username, e.getGuild().getIdLong());
			String usableString = this.getUserString(userId, e, username);

			SecondsTimeFrameCount wastedOnMusicWeek = lastFM.getMinutesWastedOnMusicWeek(username);
			sendMessage(e, "**" + usableString + "** played " + wastedOnMusicWeek
					.getMinutes() + " minutes of music this week, listening to " + wastedOnMusicWeek
					.getCount() + " different tracks in last " + wastedOnMusicWeek.getTimeFrame().toString());
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex) {
			parser.sendError("Internal Service Error, try again later", e);
		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(1), e);
		}
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
	List<String> getAliases() {
		return Collections.singletonList("!minutes");
	}
}
