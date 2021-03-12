package core.parsers.params;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;

public class DateParameters extends DiscordParameters {
    private final OffsetDateTime date;
    private final boolean isAllTime;

    public DateParameters(MessageReceivedEvent e, User user, OffsetDateTime date, boolean isAllTime) {
        super(e, user);
        this.date = date;
        this.isAllTime = isAllTime;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public boolean isAllTime() {
        return isAllTime;
    }


}
