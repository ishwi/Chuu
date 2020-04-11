package core.parsers.params;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UserParameters
        extends DiscordParameters {
    private final String lastFMId;


    public UserParameters(MessageReceivedEvent e, User user, String lastFMId) {
        super(e, user);
        this.lastFMId = lastFMId;
    }

    public String getLastFMId() {
        return lastFMId;
    }

}
