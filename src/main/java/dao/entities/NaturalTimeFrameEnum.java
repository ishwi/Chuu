package dao.entities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum NaturalTimeFrameEnum {
    YEAR("y"), QUARTER("q"), MONTH("m"), ALL("a"), SEMESTER("s"), WEEK("w"), DAY("d");

    private static final Map<String, NaturalTimeFrameEnum> ENUM_MAP;

    static {
        ENUM_MAP = Stream.of(NaturalTimeFrameEnum.values())
                .collect(Collectors.toMap(NaturalTimeFrameEnum::getName, Function.identity()));
    }

    private final String name;

    NaturalTimeFrameEnum(String name) {
        this.name = name;
    }

    public static NaturalTimeFrameEnum fromCompletePeriod(String period) {
        return switch (period) {
            case "12month" -> YEAR;
            case "3month" -> QUARTER;
            case "1month" -> MONTH;
            case "overall" -> ALL;
            case "6month" -> SEMESTER;
            case "day" -> DAY;
            default -> WEEK;
        };
    }

    public static NaturalTimeFrameEnum get(String name) {
        return ENUM_MAP.get(name);
    }

    // getter method
    private String getName() {
        return this.name;
    }

    public String toApiFormat() {
        return switch (name) {
            case "y", "yearly", "year" -> "12month";
            case "q", "quarter", "quarterly" -> "3month";
            case "m", "month", "monthly" -> "1month";
            case "a", "alltime", "overall", "all" -> "overall";
            case "s", "semester", "semesterly" -> "6month";
            case "d", "day", "daily" -> "day";
            default -> "7day";
        };
    }
}
