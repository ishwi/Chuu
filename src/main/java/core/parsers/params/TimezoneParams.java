package core.parsers.params;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.TimeZone;

public class TimezoneParams extends DiscordParameters {
    private final TimeZone timeZone;

    public TimezoneParams(MessageReceivedEvent e, User user, TimeZone timeZone) {
        super(e, user);
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
