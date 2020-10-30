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
            case "hour":
                return HOUR;
            case "minute":
                return MINUTE;
            case "second":
                return SECOND;
            default:
                return WEEK;
        }
    }

    public static NaturalTimeFrameEnum get(String name) {
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
            case "h":
            case "hour":
                return "hour";
            case "min":
                return "minute";
            case "sec":
                return "second";
            default:
                return "7day";
        }
    }

    public LocalDateTime toLocalDate(int intCount) {
        LocalDateTime localDate;
        switch (this) {
            case YEAR:
                localDate = LocalDateTime.now().minus(intCount, ChronoUnit.YEARS);
                break;
            case QUARTER:
                localDate = LocalDateTime.now().minus(3 * (long) intCount, ChronoUnit.MONTHS);
                break;
            case MONTH:
                localDate = LocalDateTime.now().minus(intCount, ChronoUnit.MONTHS);
                break;
            case ALL:
                localDate = Constants.LASTFM_CREATION_DATE.atStartOfDay();
                break;
            case SEMESTER:
                localDate = LocalDateTime.now().minus(6 * (long) intCount, ChronoUnit.MONTHS);
                break;
            case WEEK:
                localDate = LocalDateTime.now().minus(7 * (long) intCount, ChronoUnit.DAYS);
                break;
            case DAY:
                localDate = LocalDateTime.now().minus(intCount, ChronoUnit.DAYS);
                break;
            case HOUR:
                localDate = LocalDateTime.now().minus(intCount, ChronoUnit.HOURS);
                break;
            case MINUTE:
                localDate = LocalDateTime.now().minus(intCount, ChronoUnit.MINUTES);
                break;
            case SECOND:
                localDate = LocalDateTime.now().minus(intCount, ChronoUnit.SECONDS);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return localDate;
    }
}
