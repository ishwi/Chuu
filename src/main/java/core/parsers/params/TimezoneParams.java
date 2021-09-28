package core.parsers.params;

import core.commands.Context;
import net.dv8tion.jda.api.entities.User;

import java.util.TimeZone;

public class TimezoneParams extends DiscordParameters {
    private final TimeZone timeZone;
    private final boolean check;

    public TimezoneParams(Context e, User user, TimeZone timeZone) {
        this(e, user, timeZone, false);
    }

    public TimezoneParams(Context e, User user, TimeZone timeZone, boolean check) {
        super(e, user);
        this.timeZone = timeZone;
        this.check = check;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isCheck() {
        return check;
    }
}
