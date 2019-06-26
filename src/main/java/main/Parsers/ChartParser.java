package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartParser extends DaoParser {
	public ChartParser(DaoImplementation dao) {
		super(dao);
	}

	String getTimeFromChar(String timeFrame) {
		switch (timeFrame) {
			case "y":
				return "12month";
			case "q":
				return "3month";
			case "m":
				return "1month";
			case "a":
				return "overall";
			case "s":
				return "6month";
			default:
				return "7day";
		}
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {
		String timeFrame = "w";
		String discordName;
		String x = "5";
		String y = "5";

		String pattern = "\\d+[xX]\\d+";
		String[] message = getSubMessage(e.getMessage());


		boolean flag = true;
		String[] message1 = Arrays.stream(message).filter(s -> !s.equals("--artist")).toArray(String[]::new);
		if (message1.length != message.length) {
			message = message1;
			flag = false;
		}
		if (message.length > 3) {
			sendError(getErrorMessage(2), e);
			return null;
		}

		Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches(pattern));
		Optional<String> opt = firstStream.filter(s -> s.matches(pattern)).findAny();
		if (opt.isPresent()) {
			x = (opt.get().split("[xX]")[0]);
			y = opt.get().split("[xX]")[1];
			message = Arrays.stream(message).filter(s -> !s.equals(opt.get())).toArray(String[]::new);

		}

		Stream<String> secondStream = Arrays.stream(message).filter(s -> s.length() == 1 && s.matches("[yqsmwa]"));
		Optional<String> opt2 = secondStream.findAny();
		if (opt2.isPresent()) {
			timeFrame = opt2.get();
			message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);

		}

		discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (discordName == null) {

			return null;
		}

		timeFrame = getTimeFromChar(timeFrame);
		return new String[]{x, y, discordName, timeFrame, Boolean.toString(flag)};
	}


	@Override
	public void setUpErrorMessages() {
		super.setUpErrorMessages();
		errorMessages.put(2, "You Introduced too many words");
		errorMessages.put(3, "Not a valid Last.fm username");
		errorMessages.put(4, "Internal Server Error, try again later");

	}

	public String getErrorMessage(int code) {
		return errorMessages.get(code);
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList(PREFIX + commandName + "* *[w,m,q,s,y,a]* *Username* *YEAR*** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf Year not specified it default to current year\n\n"
		);
	}

}
