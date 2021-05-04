package core.parsers.params;

import core.commands.Context;

public class UserConfigParameters extends CommandParameters {
    private final UserConfigType config;
    private final String value;


    public UserConfigParameters(Context e, UserConfigType guildConfig, String value) {
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
