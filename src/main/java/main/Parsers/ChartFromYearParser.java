package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.Collections;
import java.util.List;

public class ChartFromYearParser extends ChartParser {
	private final int chartSize;
	private TimeFrameEnum defaultTFE = TimeFrameEnum.YEAR;

	public ChartFromYearParser(DaoImplementation dao, int chartSize) {
		super(dao);
		this.chartSize = chartSize;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		TimeFrameEnum timeFrame = defaultTFE;
		String discordName;
		String[] message = getSubMessage(e.getMessage());

		FlagParser flagParser = new FlagParser(message);
		boolean writeTitles = !flagParser.contains("notitles");
		boolean writePlays = flagParser.contains("plays");
		message = flagParser.getMessage();

		if (message.length > 2) {
			sendError(getErrorMessage(2), e);
			return null;
		}
		ChartParserAux chartParserAux = new ChartParserAux(message);
		String year = chartParserAux.parseYear();
		timeFrame = chartParserAux.parseTimeframe(timeFrame);
		message = chartParserAux.getMessage();

		discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (discordName == null) {

			return null;
		}
		if (Year.now().compareTo(Year.of(Integer.parseInt(year))) < 0) {
			sendError(getErrorMessage(5), e);
			return null;
		}

		return new String[]{"0", year, discordName, timeFrame.toApiFormat(), Boolean.toString(true), Boolean.toString(writeTitles), Boolean.toString(writePlays)};

	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *[w,m,q,s,y,a]* *Username* *YEAR*** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf YEAR not specified it default to current year\n" +
				"\tcan use --notitles to not display titles\n" +
				"\tcan use --plays to display plays\n\n");
	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(5, "YEAR must be current year or lower");
	}
}
