package main.Parsers;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class TimerFrameParser extends DaoParser {
	public TimerFrameParser(DaoImplementation dao) {
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

		String[] message = getSubMessage(e.getMessage());
		String timeFrame = "w";
		String discordName;

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
		return new String[]{discordName, timeFrame};
	}

}
