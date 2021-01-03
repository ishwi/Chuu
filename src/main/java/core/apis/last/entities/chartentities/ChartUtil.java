package core.apis.last.entities.chartentities;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.function.BiFunction;

public class ChartUtil {

    public static BiFunction<JSONObject, Integer, UrlCapsule> getParser(CustomTimeFrame timeFrameEnum, TopEntity topEntity, ChartParameters chartParameters, ConcurrentLastFM lastFM, LastFMData username) throws LastFmException {
        if (timeFrameEnum.getType() == CustomTimeFrame.Type.NORMAL) {
            return new TimeFrameParser(lastFM, username, chartParameters, topEntity, timeFrameEnum.getTimeFrameEnum()).obtainParse();
        } else {
            return new CustomTimeFrameParser(lastFM, username, chartParameters, topEntity, timeFrameEnum, Chuu.getDao()).obtainParse();
        }
    }

    public static Pair<Long, Long> getFromTo(CustomTimeFrame customTimeFrame) {
        long from, to;
        if (customTimeFrame.getType() == CustomTimeFrame.Type.NATURAL) {
            NaturalTimeFrameEnum naturalTimeFrameEnum = customTimeFrame.getNaturalTimeFrameEnum();
            LocalDateTime localDateTime = naturalTimeFrameEnum.toLocalDate(Math.toIntExact(customTimeFrame.getCount()));
            from = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).getEpochSecond();
            to = OffsetDateTime.now().toInstant().getEpochSecond();
        } else {
            from = customTimeFrame.getFrom().toInstant().getEpochSecond();
            to = customTimeFrame.getTo().toInstant().getEpochSecond();
        }
        return Pair.of(from, to);
    }
}
