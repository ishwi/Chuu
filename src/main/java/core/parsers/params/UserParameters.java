package core.parsers.params;

import core.commands.Context;
import net.dv8tion.jda.api.entities.User;

public class UserParameters
        extends DiscordParameters {
    private final String lastFMId;


    public UserParameters(Context e, User user, String lastFMId) {
        super(e, user);
        this.lastFMId = lastFMId;
    }

    public String getLastFMId() {
        return lastFMId;
    }

}
