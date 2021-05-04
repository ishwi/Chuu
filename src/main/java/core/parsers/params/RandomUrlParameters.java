package core.parsers.params;

import core.commands.Context;
import net.dv8tion.jda.api.entities.User;

public class RandomUrlParameters extends UrlParameters {
    private final User user;

    public RandomUrlParameters(Context e, String url, User user) {
        super(e, url);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
