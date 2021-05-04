package dao.entities;

import dao.utils.Constants;
import org.apache.commons.text.WordUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TimeFrameEnum {

    DAY("d"), WEEK("w"), MONTH("m"), QUARTER("q"), SEMESTER("s"), YEAR("y"), ALL("a");

    private static final Map<String, TimeFrameEnum> ENUM_MAP;

    static {
        ENUM_MAP = Stream.of(TimeFrameEnum.values())
                .collect(Collectors.toMap(TimeFrameEnum::getName, Function.identity()));
    }

    private final int count;
    private final String name;

    TimeFrameEnum(String name) {
        this(1, name);
    }

    TimeFrameEnum(int count, String name) {
        this.count = count;
        this.name = name;
    }

    public static TimeFrameEnum fromCompletePeriod(String period) {
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

    public static TimeFrameEnum get(String name) {
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

    public static String getDisplayString(String timefraeStr) {
        return (timefraeStr
                .equals("overall") ? " " : " in the last " + TimeFrameEnum
                .fromCompletePeriod(timefraeStr).toString().toLowerCase());
    }

    public String toValueString() {
        return WordUtils.capitalizeFully(this.name());
    }


    public String getDisplayString() {
        return (this.equals(TimeFrameEnum.ALL) ? "" : " in the last " + this.toString().toLowerCase());
    }

    public LocalDateTime toLocalDate(int count) {

        return switch (this) {
            case YEAR -> LocalDateTime.now().minus(count, ChronoUnit.YEARS);
            case QUARTER -> LocalDateTime.now().minus(3L * count, ChronoUnit.MONTHS);
            case MONTH -> LocalDateTime.now().minus(count, ChronoUnit.MONTHS);
            case ALL -> Constants.LASTFM_CREATION_DATE.atStartOfDay();
            case SEMESTER -> LocalDateTime.now().minus(6L * count, ChronoUnit.MONTHS);
            case WEEK -> LocalDateTime.now().minus(7L * count, ChronoUnit.DAYS);
            case DAY -> LocalDateTime.now().minus(count, ChronoUnit.DAYS);
        };

    }


    public int getCount() {
        return count;
    }
}



