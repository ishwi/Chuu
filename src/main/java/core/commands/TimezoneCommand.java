package core.commands;

import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimezoneParser;
import core.parsers.params.TimezoneParams;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

public class TimezoneCommand extends ConcurrentCommand<TimezoneParams> {
    public TimezoneCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<TimezoneParams> initParser() {
        return new TimezoneParser();
    }

    @Override
    public String getDescription() {
        return "Sets your timezone so some time functionality of the bot can be more accurate";
    }

    @Override
    public List<String> getAliases() {
        return List.of("timezone", "tz");
    }

    @Override
    public String getName() {
        return "Timezone Config";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        TimezoneParams parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        TimeZone timeZone = parse.getTimeZone();
        if (parse.isCheck()) {
            timeZone = getService().getUserTimezone(parse.getUser().getIdLong());
            sendMessageQueue(e, String.format("%s timezone is: %s (%s)\n Current time: %s", getUserString(e, e.getAuthor().getIdLong()), timeZone.getDisplayName(), timeZone.toZoneId().getRules().getStandardOffset(Instant.now()), DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now().atZone(timeZone.toZoneId()))));
        } else {
            getService().setTimezoneUser(timeZone, e.getAuthor().getIdLong());
            sendMessageQueue(e, String.format("Successfully set %s timezone to: %s (%s)", getUserString(e, e.getAuthor().getIdLong()), timeZone.getDisplayName(), timeZone.toZoneId().getRules().getStandardOffset(Instant.now())));

        }
    }
}
