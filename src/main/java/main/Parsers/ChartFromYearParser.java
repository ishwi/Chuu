package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartFromYearParser extends ChartParser {
	private final int chartSize;
	private TimeFrameEnum defaultTFE = TimeFrameEnum.WEEK;

	public ChartFromYearParser(DaoImplementation dao, int chartSize) {
		super(dao);
		this.chartSize = chartSize;
	}

	@Override
	protected void setUpOptionals() {
		opts.add(new OptionalEntity("--notitles", "dont display titles"));
		opts.add(new OptionalEntity("--plays", "display play count"));

	}

	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		TimeFrameEnum timeFrame = defaultTFE;
		String discordName;

		if (subMessage.length > 2) {
			sendError(getErrorMessage(5), e);
			return null;
		}
		ChartParserAux chartParserAux = new ChartParserAux(subMessage);
		String year = chartParserAux.parseYear();
		timeFrame = chartParserAux.parseTimeframe(timeFrame);
		subMessage = chartParserAux.getMessage();

		discordName = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		if (discordName == null) {

			return null;
		}
		if (Year.now().compareTo(Year.of(Integer.parseInt(year))) < 0) {
			sendError(getErrorMessage(6), e);
			return null;
		}

		return new String[]{"0", year, discordName, timeFrame.toApiFormat(), Boolean.toString(true)};

	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *[w,m,q,s,y,a]* *Username* *YEAR*** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf YEAR not specified it default to current year\n";
	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(6, "YEAR must be current year or lower");
	}
}
