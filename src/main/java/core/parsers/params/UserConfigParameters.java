package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UserConfigParameters extends CommandParameters {
    private final UserConfigType config;
    private final String value;


    public UserConfigParameters(MessageReceivedEvent e, UserConfigType guildConfig, String value) {
        super(e);
        this.config = guildConfig;
        this.value = value;
    }

    public UserConfigType getConfig() {
        return config;
    }

    public String getValue() {
        return value;
    }
}
