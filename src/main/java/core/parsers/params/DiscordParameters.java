package core.parsers.params;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordParameters extends CommandParameters {
    private final User user;

    public DiscordParameters(MessageReceivedEvent e, User user) {
        super(e);
        this.user = user;
    }


    public User getUser() {
        return user;
    }
}
