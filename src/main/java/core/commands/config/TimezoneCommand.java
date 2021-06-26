package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.TimezoneParser;
import core.parsers.params.TimezoneParams;
import dao.ServiceView;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

public class TimezoneCommand extends ConcurrentCommand<TimezoneParams> {
    public TimezoneCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<TimezoneParams> initParser() {
        return new TimezoneParser(db);
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
    protected void onCommand(Context e, @NotNull TimezoneParams params) {


        TimeZone timeZone = params.getTimeZone();
        if (params.isCheck()) {
            long id = params.getUser().getIdLong();
            timeZone = db.getUserTimezone(id);
            sendMessageQueue(e, String.format("%s timezone is: %s (%s)\n Current time: %s", getUserString(e, id), timeZone.getDisplayName(), timeZone.toZoneId().getRules().getStandardOffset(Instant.now()), DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now().atZone(timeZone.toZoneId()))));
        } else {
            db.setTimezoneUser(timeZone, e.getAuthor().getIdLong());
            sendMessageQueue(e, String.format("Successfully set %s timezone to: %s (%s)", getUserString(e, e.getAuthor().getIdLong()), timeZone.getDisplayName(), timeZone.toZoneId().getRules().getStandardOffset(Instant.now())));

        }
    }
}
