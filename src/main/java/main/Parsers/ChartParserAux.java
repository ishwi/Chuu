package main.Parsers;

import DAO.Entities.TimeFrameEnum;

import java.time.Year;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartParserAux {
	String[] message;

	public ChartParserAux(String[] message) {
		this.message = message;
	}

	public String[] getMessage() {
		return message;
	}

	public TimeFrameEnum parseTimeframe(TimeFrameEnum defaultTimeFrame) {
		TimeFrameEnum timeFrame = defaultTimeFrame;
		Stream<String> secondStream = Arrays.stream(message).filter(s -> s.length() == 1 && s.matches("[yqsmwa]"));
		Optional<String> opt2 = secondStream.findAny();
		if (opt2.isPresent()) {
			timeFrame = TimeFrameEnum.get(opt2.get());
			message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);
		}
		return timeFrame;
	}


	public String parseYear() {
		String year = Year.now().toString();
		Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches("\\d{4}"));
		Optional<String> opt1 = firstStream.findAny();
		if (opt1.isPresent()) {
			year = opt1.get();
			message = Arrays.stream(message).filter(s -> !s.equals(opt1.get().trim())).toArray(String[]::new);

		}
		return year;
	}

}
