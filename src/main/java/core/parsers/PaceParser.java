package core.parsers;

import dao.ChuuService;
import dao.entities.NaturalTimeFrameEnum;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class PaceParser extends NumberParser<ExtraParser<NaturalTimeFrameParser, Long>> {

    public final static LocalDate LASTFM_CREATION_DATE = LocalDate.of(2002, 2, 20);

    // Dont ask
    public PaceParser(ChuuService dao, Map<Integer, String> errorMessages) {
        super(new ExtraParser<>(
                        new NaturalTimeFrameParser(dao, NaturalTimeFrameEnum.ALL),
                        null,
                        NumberParser.predicate,
                        (x) -> false,
                        Long::parseLong,
                        String::valueOf,
                        errorMessages,
                        "*scrobble_goal*",
                        "Number of scrobles that will be needed to hit the goal",
                        (arr, number) -> {
                            NaturalTimeFrameEnum naturalTimeFrameEnum = NaturalTimeFrameEnum.fromCompletePeriod(arr[2]);
                            LocalDate now = LocalDate.now();
                            switch (naturalTimeFrameEnum) {
                                case YEAR:
                                    return number > (int) ChronoUnit.YEARS.between(LASTFM_CREATION_DATE, now);
                                case QUARTER:
                                    return number > (int) ChronoUnit.YEARS.between(LASTFM_CREATION_DATE, now) * 4;
                                case MONTH:
                                    return number > ChronoUnit.MONTHS.between(LASTFM_CREATION_DATE, now);
                                case ALL:
                                    return false;
                                case SEMESTER:
                                    return number > ChronoUnit.YEARS.between(LASTFM_CREATION_DATE, now) * 2;
                                case WEEK:
                                    return number > ChronoUnit.WEEKS.between(LASTFM_CREATION_DATE, now);
                                case DAY:
                                    return number > ChronoUnit.DAYS.between(LASTFM_CREATION_DATE, now);
                                case HOUR:
                                    return number > ChronoUnit.HOURS.between(LASTFM_CREATION_DATE, now);
                                case MINUTE:
                                    return number > ChronoUnit.MINUTES.between(LASTFM_CREATION_DATE, now);
                                case SECOND:
                                    return number > ChronoUnit.SECONDS.between(LASTFM_CREATION_DATE, now);
                                default:
                                    throw new IllegalArgumentException();
                            }
                        })
                , null, Long.MAX_VALUE, new HashMap<>(), "Number represents the number of periods of the specified timeframe", false, (list) -> list.stream().mapToLong(Long::longValue).max().getAsLong());
    }
    // [0] -> NumberParserResult [1] -> ExtraParser of Natural
    //
}

