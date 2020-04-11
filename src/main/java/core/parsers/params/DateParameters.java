package core.parsers.params;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;

public class DateParameters extends DiscordParameters {
    private final OffsetDateTime date;

    public DateParameters(MessageReceivedEvent e, User user, OffsetDateTime date) {
        super(e, user);
        this.date = date;
    }

    public OffsetDateTime getDate() {
        return date;
    }

}
