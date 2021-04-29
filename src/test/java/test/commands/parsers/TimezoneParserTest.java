package test.commands.parsers;

import core.exceptions.LastFmException;
import core.parsers.TimezoneParser;
import core.parsers.params.TimezoneParams;
import dao.exceptions.InstanceNotFoundException;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.*;

public class TimezoneParserTest {
    @Test
    public void name() throws LastFmException, InstanceNotFoundException {
        TimezoneParser timezoneParser = new TimezoneParser(null);

        assertTrue(timezoneParser.parse(new MessageGenerator().generateMessage("!timezone")).isCheck());
        assertThrows(NullPointerException.class,
                () -> timezoneParser.parse(new MessageGenerator().generateMessage("!timezone   asdzcasd")));
        TimezoneParams parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone Madrid"));

        assertEquals(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("+01:00").getId());

        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone +1"));
        assertEquals(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("+01:00").getId());

        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone europe madrid"));
        assertEquals(parse.getTimeZone().getID(), "Europe/Madrid");

        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone CET"));
        assertEquals(parse.getTimeZone().getID(), "CET");


    }

    @Test
    public void issue() throws LastFmException, InstanceNotFoundException {
        TimezoneParser timezoneParser = new TimezoneParser(null);


        TimezoneParams params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone America/Denver"));
        assertEquals(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("-07:00").getId());


        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone MST"));
        assertEquals(params.getTimeZone().getID(), "MST");

        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone -07:00"));
        assertEquals(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("-07:00").getId());

        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone Australia/Sydney"));
        assertEquals(params.getTimeZone().getID(), "Australia/Sydney");

//        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone AEST"));
//        assertEquals(params.getTimeZone().getID(), "AEST");

        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone GMT-07:00"));
        assertEquals(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("-07:00").getId());

        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone UTC-07:00"));
        assertEquals(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId(), ZoneId.of("-07:00").getId());


    }
}
