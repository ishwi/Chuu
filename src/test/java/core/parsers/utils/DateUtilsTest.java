package core.parsers.utils;

import core.parsers.ChartParserAux;
import core.parsers.exceptions.InvalidDateException;
import dao.entities.TimeFrameEnum;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilsTest {

    @Test
    public void name() {
        DateUtils dateUtils = new DateUtils();
        DateUtils.DateParsed dateParsed1 = dateUtils.parseOnlyOne(new String[]{"January"});
        OffsetDateTime expected = OffsetDateTime.of(Year.now().getValue(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime january = dateParsed1.from();
        assertThat(january.getYear()).isEqualTo(expected.getYear());
        assertThat(january.getMonth()).isEqualTo(expected.getMonth());
        DateUtils.DateParsed dateParsed = dateUtils.parseString("january - march".split(" "));
        assertThat(dateParsed.from()).isNotNull();
        assertThat(dateParsed.to()).isNotNull();
        assertThat(dateParsed.remainingWords().length).isEqualTo(0);


    }

    @Test
    public void name2() throws InvalidDateException {
        ChartParserAux parserAux = new ChartParserAux(new String[]{"January"});
        CustomTimeFrame customTimeFrame = parserAux.parseCustomTimeFrame(TimeFrameEnum.ALL);

    }
}
