package DAO.Entities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TimeFrameEnum {

	YEAR("y"), QUARTER("q"), MONTH("m"), ALL("a"), SEMESTER("s"), WEEK("w");

	private static final Map<String, TimeFrameEnum> ENUM_MAP;

	static {
		ENUM_MAP = Stream.of(TimeFrameEnum.values()).collect(Collectors.toMap(Enum::name, Function.identity()));
	}

	private String name;

	TimeFrameEnum(String name) {
		this.name = name;
	}

	public static TimeFrameEnum get(String name) {
		return ENUM_MAP.get(name);
	}

	// getter method
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		switch (name) {
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


}



