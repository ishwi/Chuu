package dao.entities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TimeFrameEnum {

    YEAR("y"), QUARTER("q"), MONTH("m"), ALL("a"), SEMESTER("s"), WEEK("w");

    private static final Map<String, TimeFrameEnum> ENUM_MAP;

    static {
        ENUM_MAP = Stream.of(TimeFrameEnum.values())
                .collect(Collectors.toMap(TimeFrameEnum::getName, Function.identity()));
    }

    private final String name;

    TimeFrameEnum(String name) {
        this.name = name;
    }

    public static TimeFrameEnum fromCompletePeriod(String period) {
        switch (period) {
            case "12month":
                return YEAR;
            case "3month":
                return QUARTER;
            case "1month":
                return MONTH;
            case "overall":
                return ALL;
            case "6month":
                return SEMESTER;
            default:
                return WEEK;
        }
    }

    public static TimeFrameEnum get(String name) {
        return ENUM_MAP.get(name);
    }

    // getter method
    private String getName() {
        return this.name;
    }

    public String toApiFormat() {
        switch (name) {
            case "y":
            case "yearly":
            case "year":
                return "12month";
            case "q":
            case "quarter":
            case "quarterly":
                return "3month";
            case "m":
            case "month":
            case "monthly":
                return "1month";
            case "a":
            case "alltime":
            case "overall":
            case "all":
                return "overall";
            case "s":
            case "semester":
            case "semesterly":
                return "6month";
            default:
                return "7day";
        }
    }

    public static String getDisplayString(String timefraeStr) {
        return (timefraeStr
                .equals("overall") ? " " : " in the last " + TimeFrameEnum
                .fromCompletePeriod(timefraeStr).toString().toLowerCase());
    }

    public String getDisplayString() {
        return (this.equals(TimeFrameEnum.ALL) ? "" : " in the last " + this.toString().toLowerCase());

    }

}



