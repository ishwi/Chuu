package dao.entities;


import dao.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum NaturalTimeFrameEnum {
    YEAR("y"), QUARTER("q"), MONTH("m"), ALL("a"), SEMESTER("s"), WEEK("w"), DAY("d"), HOUR("h"), MINUTE("min"), SECOND("sec");

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
            case "hour" -> HOUR;
            case "minute" -> MINUTE;
            case "second" -> SECOND;
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
            case "h", "hour" -> "hour";
            case "min" -> "minute";
            case "sec" -> "second";
            default -> "7day";
        };
    }

    public LocalDateTime toLocalDate(int intCount) {
        return switch (this) {
            case YEAR -> LocalDateTime.now().minus(intCount, ChronoUnit.YEARS);
            case QUARTER -> LocalDateTime.now().minus(3 * (long) intCount, ChronoUnit.MONTHS);
            case MONTH -> LocalDateTime.now().minus(intCount, ChronoUnit.MONTHS);
            case ALL -> Constants.LASTFM_CREATION_DATE.atStartOfDay();
            case SEMESTER -> LocalDateTime.now().minus(6 * (long) intCount, ChronoUnit.MONTHS);
            case WEEK -> LocalDateTime.now().minus(7 * (long) intCount, ChronoUnit.DAYS);
            case DAY -> LocalDateTime.now().minus(intCount, ChronoUnit.DAYS);
            case HOUR -> LocalDateTime.now().minus(intCount, ChronoUnit.HOURS);
            case MINUTE -> LocalDateTime.now().minus(intCount, ChronoUnit.MINUTES);
            case SECOND -> LocalDateTime.now().minus(intCount, ChronoUnit.SECONDS);
        };
    }

    public String getDisplayString(long count) {

        String name;
        if (count == 1) {
            name = this.toString().toLowerCase();
        } else {
            name = this.toString().toLowerCase() + "s";
        }
        return (this.equals(NaturalTimeFrameEnum.ALL) ? "" : String.format(" in the last %d %s", count, name));

    }
}
