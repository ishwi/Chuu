package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UserParameters
        extends CommandParameters {
    private final long discordID;
    private final String lastFMId;


    public UserParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        super(message, e, opts);
        this.lastFMId = message[0];
        this.discordID = Long.parseLong(message[1]);
    }

    public String getLastFMId() {
        return lastFMId;
    }

    public long getDiscordID() {
        return discordID;
    }
}
