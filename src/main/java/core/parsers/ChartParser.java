package core.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import core.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartParser extends DaoParser {
	private final TimeFrameEnum defaultTFE = TimeFrameEnum.WEEK;

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
	public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
		TimeFrameEnum timeFrame = defaultTFE;
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
			if (x.equals("0") || y.equals("0")){
				sendError(getErrorMessage(6),e);
				return null;
			}
		}

		ChartParserAux chartParserAux = new ChartParserAux(subMessage);
		timeFrame = chartParserAux.parseTimeframe(timeFrame);
		subMessage = chartParserAux.getMessage();

		LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);

		return new String[]{x, y, data.getName(), timeFrame.toApiFormat()};
	}


	public String getErrorMessage(int code) {
		return errorMessages.get(code);
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *[w,m,q,s,y,a]* *sizeXsize*  *Username* ** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf Size not specified it defaults to 5x5\n";
	}

	@Override
	protected void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(5, "You Introduced too many words");
		errorMessages.put(6, "0 is not a valid value for a chart!");

	}

}
