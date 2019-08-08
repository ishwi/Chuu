package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartParser extends DaoParser {
	private TimeFrameEnum defaultTFE = TimeFrameEnum.WEEK;

	public ChartParser(DaoImplementation dao) {
		super(dao);
	}

	@Override
	protected void setUpOptionals() {
		opts.add(new OptionalEntity("--artist", "use artist instead of albums"));
		opts.add(new OptionalEntity("--notitles", "dont display titles"));
		opts.add(new OptionalEntity("--plays", "display play count"));
	}

	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
		TimeFrameEnum timeFrame = defaultTFE;
		String discordName;
		String x = "5";
		String y = "5";

		String pattern = "\\d+[xX]\\d+";

		if (subMessage.length > 3) {
			sendError(getErrorMessage(5), e);
			return null;
		}

		Stream<String> firstStream = Arrays.stream(subMessage).filter(s -> s.matches(pattern));
		Optional<String> opt = firstStream.filter(s -> s.matches(pattern)).findAny();
		if (opt.isPresent()) {
			x = (opt.get().split("[xX]")[0]);
			y = opt.get().split("[xX]")[1];
			subMessage = Arrays.stream(subMessage).filter(s -> !s.equals(opt.get())).toArray(String[]::new);

		}

		ChartParserAux chartParserAux = new ChartParserAux(subMessage);
		timeFrame = chartParserAux.parseTimeframe(timeFrame);
		subMessage = chartParserAux.getMessage();

		discordName = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);
		if (discordName == null) {
			return null;
		}

		return new String[]{x, y, discordName, timeFrame.toApiFormat()};
	}


	public String getErrorMessage(int code) {
		return errorMessages.get(code);
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *[w,m,q,s,y,a]* *sizeXsize*  *Username* ** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf Size not specified it defaults  to 5x5\n";
	}

	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();

		errorMessages.put(5, "You Introduced too many words");

	}

}
