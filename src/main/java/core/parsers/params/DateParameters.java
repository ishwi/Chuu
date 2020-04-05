package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateParameters extends CommandParameters {
    private final OffsetDateTime date;
    private final long discordId;

    public DateParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        super(message, e, opts);
        this.discordId = Long.parseLong(message[0]);
        this.date = DateTimeFormatter.ISO_DATE_TIME.parse(message[1], OffsetDateTime::from);
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public long getDiscordId() {
        return discordId;
    }
}
