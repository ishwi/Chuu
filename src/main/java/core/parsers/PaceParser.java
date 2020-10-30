package core.parsers;

import core.parsers.params.NaturalTimeParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.NaturalTimeFrameEnum;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static dao.utils.Constants.LASTFM_CREATION_DATE;

// I Have sinned
//

public class PaceParser extends NumberParser<NumberParameters<NaturalTimeParams>, NumberParser<NaturalTimeParams, NaturalTimeFrameParser>> {


    // Dont ask
    public PaceParser(ChuuService dao, Map<Integer, String> errorMessages) {
        super(
                new NumberParser<>
                        (new NaturalTimeFrameParser(dao, NaturalTimeFrameEnum.ALL)
                                , null,
                                Long.MAX_VALUE
                                , errorMessages,
                                "The total number of scrobbles you want to see",
                                "Scrobble Goal",
                                (item, number) -> {
                                    NaturalTimeFrameEnum naturalTimeFrameEnum = item.getTime();
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
                                }
                        ), null,
                Long.MAX_VALUE,
                new HashMap<>(),
                "Number that represents the number of periods of the specified timeframe",
                false,
                list -> list.stream().
                        mapToLong(Long::longValue).
                        max().orElse(0));
    }
// [0] -> NumberParserResult [1] -> ExtraParser of Natural
//


}

