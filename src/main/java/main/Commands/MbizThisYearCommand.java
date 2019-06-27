package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.OnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;

public class MbizThisYearCommand extends MusicBrainzCommand {
	private final int chartSize = 150;

	public MbizThisYearCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new OnlyUsernameParser(dao);
	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;


		String username = returned[0];


		LocalDateTime time = LocalDateTime.now();
		String timeframe;
		int monthValue = time.getMonthValue();
		if (monthValue == 1 && time.getDayOfMonth() < 8)
			timeframe = "7day";
		if (monthValue < 2) {
			timeframe = "1month";
		} else if (monthValue < 4) {
			timeframe = "3month";
		} else if (monthValue < 7)
			timeframe = "6month";
		else {
			timeframe = "12month";
		}
		try {
			processQueue(username, timeframe, 1, Year.now().getValue(), e);


		} catch (LastFmEntityNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmException ex2) {
			parser.sendError(parser.getErrorMessage(4), e);
		}

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!sincestartyear");
	}

	@Override
	public String getDescription() {
		return "Gets your top albums since the year started that were released in the current year ,";
	}

	@Override
	public String getName() {
		return "Since Year Started";
	}


}
