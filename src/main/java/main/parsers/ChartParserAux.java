package main.parsers;

import dao.entities.TimeFrameEnum;

import java.time.Year;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

class ChartParserAux {
	private String[] message;

	ChartParserAux(String[] message) {
		this.message = message;
	}

	public String[] getMessage() {
		return message;
	}

	TimeFrameEnum parseTimeframe(TimeFrameEnum defaultTimeFrame) {
		TimeFrameEnum timeFrame = defaultTimeFrame;
		Stream<String> secondStream = Arrays.stream(message).filter(s -> s.length() == 1 && s.matches("[yqsmwa]"));
		Optional<String> opt2 = secondStream.findAny();
		if (opt2.isPresent()) {
			timeFrame = TimeFrameEnum.get(opt2.get());
			message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);
		}
		return timeFrame;
	}


	String parseYear() {
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
