package test.commands.parsers;

import core.exceptions.LastFmException;
import core.parsers.TimezoneParser;
import core.parsers.params.TimezoneParams;
import dao.exceptions.InstanceNotFoundException;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TimezoneParserTest {
    @Test
    public void name() throws LastFmException, InstanceNotFoundException {
        TimezoneParser timezoneParser = new TimezoneParser();
        assertThrows(NullPointerException.class,
                () -> timezoneParser.parse(new MessageGenerator().generateMessage("!timezone")));
        assertThrows(NullPointerException.class,
                () -> timezoneParser.parse(new MessageGenerator().generateMessage("!timezone   asdzcasd")));
        TimezoneParams parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone Madrid"));

        assertEquals(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("+01:00").getId());

        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone +1"));
        assertEquals(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("+01:00").getId());

        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone europe madrid"));
        assertEquals(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("+01:00").getId());

        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone CET"));
        assertEquals(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("+01:00").getId());

    }
}
