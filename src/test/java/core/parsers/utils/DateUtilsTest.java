package core.parsers.utils;

import core.parsers.ChartParserAux;
import core.parsers.exceptions.InvalidDateException;
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
        OffsetDateTime january = dateParsed1.from();
        assertEquals(expected.getYear(), january.getYear());
        assertEquals(expected.getMonth(), january.getMonth());
        DateUtils.DateParsed dateParsed = dateUtils.parseString("january - march".split(" "));
        assertNotNull(dateParsed.from());
        assertNotNull(dateParsed.to());
        assertEquals(0, dateParsed.remainingWords().length);


    }

    @Test
    public void name2() throws InvalidDateException {
        ChartParserAux parserAux = new ChartParserAux(new String[]{"January"});
        CustomTimeFrame customTimeFrame = parserAux.parseCustomTimeFrame(TimeFrameEnum.ALL);

    }
}
