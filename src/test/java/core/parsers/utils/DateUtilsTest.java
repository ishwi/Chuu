package core.parsers.utils;

import core.parsers.ChartParserAux;
import dao.entities.TimeFrameEnum;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateUtilsTest {

    @Test
    public void name() {
        DateUtils dateUtils = new DateUtils();
        DateUtils.DateParsed dateParsed1 = dateUtils.parseOnlyOne(new String[]{"January"});
        OffsetDateTime expected = OffsetDateTime.of(Year.now().getValue(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime january = dateParsed1.getFrom();
        assertEquals(expected.getYear(), january.getYear());
        assertEquals(expected.getMonth(), january.getMonth());
        DateUtils.DateParsed dateParsed = dateUtils.parseString("january - march".split(" "));
        assertNotNull(dateParsed.getFrom());
        assertNotNull(dateParsed.getTo());
        assertEquals(0, dateParsed.getRemainingWords().length);


    }

    @Test
    public void name2() {
        ChartParserAux parserAux = new ChartParserAux(new String[]{"January"});
        CustomTimeFrame customTimeFrame = parserAux.parseCustomTimeFrame(TimeFrameEnum.ALL);

    }
}
