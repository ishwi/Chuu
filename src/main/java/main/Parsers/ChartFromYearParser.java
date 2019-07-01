package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartFromYearParser extends ChartParser {
	private final int chartSize;

	public ChartFromYearParser(DaoImplementation dao, int chartSize) {
		super(dao);
		this.chartSize = chartSize;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String timeFrame = "y";
		String discordName;
		String chartSize = Integer.toString(this.chartSize);
		String year = Year.now().toString();
		String pattern = "\\d+[xX]\\d+";
		String[] message = getSubMessage(e.getMessage());


		if (message.length > 3) {
			sendError(getErrorMessage(2), e);
			return null;
		}


		Stream<String> secondStream = Arrays.stream(message).filter(s -> s.length() == 1 && s.matches("[yqsmwa]"));
		Optional<String> opt2 = secondStream.findAny();
		if (opt2.isPresent()) {
			timeFrame = opt2.get();
			message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);

		}

		Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches("\\d{4}"));
		Optional<String> opt1 = firstStream.findAny();
		if (opt1.isPresent()) {
			year = opt1.get();
			message = Arrays.stream(message).filter(s -> !s.equals(opt1.get().trim())).toArray(String[]::new);

		}

		discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (discordName == null) {

			return null;
		}
		if (Year.now().compareTo(Year.of(Integer.parseInt(year))) < 0) {
			sendError(getErrorMessage(5), e);
			return null;
		}

		timeFrame = getTimeFromChar(timeFrame);
		return new String[]{"0", year, discordName, timeFrame, Boolean.toString(true), "true", "true"};

	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(5, "Year must be current year or lower");
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *[w,m,q,s,y,a]* *Username* *YEAR*** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf Year not specified it default to current year\n\n"
		);
	}
}
