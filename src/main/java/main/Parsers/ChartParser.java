package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartParser extends DaoParser {
	private TimeFrameEnum defaultTFE = TimeFrameEnum.WEEK;

	public ChartParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		TimeFrameEnum timeFrame = defaultTFE;
		String discordName;
		String x = "5";
		String y = "5";

		String pattern = "\\d+[xX]\\d+";
		String[] message = getSubMessage(e.getMessage());

		FlagParser flagParser = new FlagParser(message);

		boolean flag = !flagParser.contains("artist");
		boolean writeTitles = !flagParser.contains("notitles");
		boolean writePlays = flagParser.contains("plays");

		message = flagParser.getMessage();
		if (message.length > 3) {
			sendError(getErrorMessage(5), e);
			return null;
		}

		Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches(pattern));
		Optional<String> opt = firstStream.filter(s -> s.matches(pattern)).findAny();
		if (opt.isPresent()) {
			x = (opt.get().split("[xX]")[0]);
			y = opt.get().split("[xX]")[1];
			message = Arrays.stream(message).filter(s -> !s.equals(opt.get())).toArray(String[]::new);

		}

		ChartParserAux chartParserAux = new ChartParserAux(message);
		timeFrame = chartParserAux.parseTimeframe(timeFrame);
		message = chartParserAux.getMessage();

		discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (discordName == null) {
			return null;
		}

		return new String[]{x, y, discordName, timeFrame.toApiFormat(), Boolean.toString(flag), Boolean.toString(writeTitles), Boolean.toString(writePlays)};
	}


	public String getErrorMessage(int code) {
		return errorMessages.get(code);
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *[w,m,q,s,y,a]* *sizeXsize*  *Username* ** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf Size not specified it to 5x5\n" +
				"\tcan use --artist to make an artist chart\n" +
				"\tcan use --notitles to not display titles\n" +
				"\tcan use --plays to display plays\n\n");

	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();

		errorMessages.put(5, "You Introduced too many words");

	}

}
