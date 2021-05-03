package core.parsers.params;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RandomUrlParameters extends UrlParameters {
    private final User user;

    public RandomUrlParameters(MessageReceivedEvent e, String url, User user) {
        super(e, url);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
