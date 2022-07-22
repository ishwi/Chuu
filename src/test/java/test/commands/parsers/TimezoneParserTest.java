//package test.commands.parsers;
//
//import core.exceptions.LastFmException;
//import core.parsers.TimezoneParser;
//import core.parsers.params.TimezoneParams;
//import dao.exceptions.InstanceNotFoundException;
//import org.junit.jupiter.api.Test;
//
//import java.time.Instant;
//import java.time.ZoneId;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//public class TimezoneParserTest {
//    @Test
//    public void name() throws LastFmException, InstanceNotFoundException {
////        TimezoneParser timezoneParser = new TimezoneParser(null);
////
////        assertThat(timezoneParser.parse(new MessageGenerator().generateMessage("!timezone")).isCheck()).isTrue();
////        assertThatThrownBy(() -> timezoneParser.parse(new MessageGenerator().generateMessage("!timezone   asdzcasd"))).isInstanceOf(NullPointerException.class);
////        TimezoneParams parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone Madrid"));
////
////        assertThat(ZoneId.of("+01:00").getId()).isEqualTo(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId());
////
////        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone +1"));
////        assertThat(ZoneId.of("+01:00").getId()).isEqualTo(parse.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId());
////
////        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone europe madrid"));
////        assertThat("Europe/Madrid").isEqualTo(parse.getTimeZone().getID());
////
////        parse = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone CET"));
////        assertThat("CET").isEqualTo(parse.getTimeZone().getID());
//
//
//    }
//
//    @Test
//    public void issue() throws LastFmException, InstanceNotFoundException {
//        TimezoneParser timezoneParser = new TimezoneParser(null);
//
//
//        TimezoneParams params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone America/Denver"));
//        assertThat(ZoneId.of("-07:00").getId()).isEqualTo(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId());
//
//
//        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone MST"));
//        assertThat("MST").isEqualTo(params.getTimeZone().getID());
//
//        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone -07:00"));
//        assertThat(ZoneId.of("-07:00").getId()).isEqualTo(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId());
//
//        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone Australia/Sydney"));
//        assertThat("Australia/Sydney").isEqualTo(params.getTimeZone().getID());
//
//
//        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone GMT-07:00"));
//        assertThat(ZoneId.of("-07:00").getId()).isEqualTo(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId());
//
//        params = timezoneParser.parse(new MessageGenerator().generateMessage("!timezone UTC-07:00"));
//        assertThat(ZoneId.of("-07:00").getId()).isEqualTo(params.getTimeZone().toZoneId().normalized().getRules().getStandardOffset(Instant.now()).getId());
//
//
//    }
//}
