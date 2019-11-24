package main.parsers;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import main.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartFromYearVariableParser extends DaoParser {
	public ChartFromYearVariableParser(DaoImplementation dao) {
		super(dao);
	}


	@Override
	void setUpOptionals() {
		opts.add(new OptionalEntity("--notitles", "dont display titles"));
		opts.add(new OptionalEntity("--plays", "display play count"));
		opts.add(new OptionalEntity("--nolimit", "makes the chart as big as possible"));
	}

	@Override
	protected String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
		LastFMData discordName;

		if (words.length > 3) {
			sendError(getErrorMessage(5), e);
			return null;
		}

		String pattern = "\\d+[xX]\\d+";
		String x = "5";
		String y = "5";
		Stream<String> firstStream = Arrays.stream(words).filter(s -> s.matches(pattern));
		Optional<String> opt = firstStream.filter(s -> s.matches(pattern)).findAny();

		boolean conflictFlag = e.getMessage().getContentRaw().contains("--nolimit");
		if (opt.isPresent()) {
			if (conflictFlag) {
				sendError(getErrorMessage(7), e);
				return null;
			}
			x = (opt.get().split("[xX]")[0]);
			y = opt.get().split("[xX]")[1];
			words = Arrays.stream(words).filter(s -> !s.equals(opt.get())).toArray(String[]::new);
		}

		ChartParserAux chartParserAux = new ChartParserAux(words);
		String year = chartParserAux.parseYear();
		words = chartParserAux.getMessage();

		discordName = getLastFmUsername1input(words, e.getAuthor().getIdLong(), e);
		if (Year.now().compareTo(Year.of(Integer.parseInt(year))) < 0) {
			sendError(getErrorMessage(6), e);
			return null;
		}
		return new String[]{x, y, year, discordName.getName()};
	}

	@Override
	String getUsageLogic(String commandName) {
		return "**" + commandName + " *Username* *YEAR* *sizeXsize*** \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf YEAR not specified it default to current year\n" +
				"\tIf Size not specified it defaults to 5x5\n";
	}

	@Override
	protected void setUpErrorMessages() {
		errorMessages.put(5, "You Introduced too many words!");
		errorMessages.put(6, "YEAR must be current year or lower");
		errorMessages.put(7, "Cant use a size for the chart if you specify the --nolimit flag!");
	}
}
