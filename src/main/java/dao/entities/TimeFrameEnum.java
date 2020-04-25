package dao.entities;

import core.parsers.PaceParser;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TimeFrameEnum {

    YEAR("y"), QUARTER("q"), MONTH("m"), ALL("a"), SEMESTER("s"), WEEK("w"), DAY("d");

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
            case "day":
                return DAY;
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
            case "d":
            case "day":
            case "daily":
                return "day";
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

    public LocalDateTime toLocalDate(int count) {

        LocalDateTime localDate;
        switch (this) {
            case YEAR:
                localDate = LocalDateTime.now().minus(count, ChronoUnit.YEARS);
                break;
            case QUARTER:
                localDate = LocalDateTime.now().minus(3L * count, ChronoUnit.MONTHS);
                break;
            case MONTH:
                localDate = LocalDateTime.now().minus(count, ChronoUnit.MONTHS);
                break;
            case ALL:
                localDate = PaceParser.LASTFM_CREATION_DATE.atStartOfDay();
                break;
            case SEMESTER:
                localDate = LocalDateTime.now().minus(6L * count, ChronoUnit.MONTHS);
                break;
            case WEEK:
                localDate = LocalDateTime.now().minus(7L * count, ChronoUnit.DAYS);
                break;
            case DAY:
                localDate = LocalDateTime.now().minus(count, ChronoUnit.DAYS);

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return localDate;

    }


    public int getCount() {
        return count;
    }
}



