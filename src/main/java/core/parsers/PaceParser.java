package core.parsers;

import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.NaturalTimeParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.NaturalTimeFrameEnum;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dao.utils.Constants.LASTFM_CREATION_DATE;

// I Have sinned
//

public class PaceParser extends NumberParser<NumberParameters<NaturalTimeParams>, NumberParser<NaturalTimeParams, NaturalTimeFrameParser>> {


    // Dont ask
    public PaceParser(ChuuService dao, Map<Integer, String> errorMessages) {
        super(
                new NumberParser<>
                        (new NaturalTimeFrameParser(dao, NaturalTimeFrameEnum.ALL),
                                null,
                                Long.MAX_VALUE,
                                errorMessages,
                                new ExplanationLineType("scrobble-goal", "The total number of scrobbles you want to see", OptionType.INTEGER),
                                PaceParser::matches, true),
                null,
                Long.MAX_VALUE,
                new HashMap<>(),
                "Number that represents the number of periods of the specified timeframe",
                false,
                PaceParser::chooser, "unit-count");

    }

    private static boolean matches(NaturalTimeParams item, Long number) {
        NaturalTimeFrameEnum naturalTimeFrameEnum = item.getTime();
        LocalDate now = LocalDate.now();
        return switch (naturalTimeFrameEnum) {
            case YEAR -> number > (int) ChronoUnit.YEARS.between(LASTFM_CREATION_DATE, now);
            case QUARTER -> number > (int) ChronoUnit.YEARS.between(LASTFM_CREATION_DATE, now) * 3L;
            case MONTH -> number > ChronoUnit.MONTHS.between(LASTFM_CREATION_DATE, now);
            case ALL -> false;
            case SEMESTER -> number > ChronoUnit.YEARS.between(LASTFM_CREATION_DATE, now) * 2;
            case WEEK -> number > ChronoUnit.WEEKS.between(LASTFM_CREATION_DATE, now);
            case DAY -> number > ChronoUnit.DAYS.between(LASTFM_CREATION_DATE, now);
            case HOUR -> number > ChronoUnit.HOURS.between(LASTFM_CREATION_DATE, now);
            case MINUTE -> number > ChronoUnit.MINUTES.between(LASTFM_CREATION_DATE, now);
            case SECOND -> number > ChronoUnit.SECONDS.between(LASTFM_CREATION_DATE, now);
        };
    }

    private static Long chooser(List<Long> list) {
        return list.stream().
                mapToLong(Long::longValue).
                max().orElse(0);
    }
// [0] -> NumberParserResult [1] -> ExtraParser of Natural
//


}

