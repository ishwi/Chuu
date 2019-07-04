package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.TimeFrameEnum;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.ChartOnlyUsernameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;

public class MbizThisYearCommand extends MusicBrainzCommand {
	private final int chartSize = 150;

	public MbizThisYearCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ChartOnlyUsernameParser(dao);
	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		String username = returned[0];
		boolean writeTitles = Boolean.valueOf(returned[1]);
		boolean writePlays = Boolean.valueOf(returned[2]);

		LocalDateTime time = LocalDateTime.now();
		TimeFrameEnum timeframe;
		int monthValue = time.getMonthValue();
		if (monthValue == 1 && time.getDayOfMonth() < 8) {
			timeframe = TimeFrameEnum.WEEK;
		} else if (monthValue < 2) {
			timeframe = TimeFrameEnum.MONTH;
		} else if (monthValue < 4) {
			timeframe = TimeFrameEnum.QUARTER;
		} else if (monthValue < 7)
			timeframe = TimeFrameEnum.SEMESTER;
		else {
			timeframe = TimeFrameEnum.YEAR;
		}

		try {
			processQueue(username, timeframe.toString(), 1, Year.now().getValue(), e, writeTitles, writePlays);

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
		return "Since YEAR Started";
	}


}
