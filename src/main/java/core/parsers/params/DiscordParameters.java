package core.parsers.params;

import core.commands.Context;
import net.dv8tion.jda.api.entities.User;

public class DiscordParameters extends CommandParameters {
    private final User user;

    public DiscordParameters(Context e, User user) {
        super(e);
        this.user = user;
    }


    public User getUser() {
        return user;
    }
}
