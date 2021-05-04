package core.parsers.params;

import core.commands.Context;

public class GuildConfigParams extends CommandParameters {
    private final GuildConfigType config;
    private final String value;

    public GuildConfigParams(Context e, GuildConfigType guildConfig, String value) {
        super(e);
        this.config = guildConfig;
        this.value = value;
    }

    public GuildConfigType getConfig() {
        return config;
    }

    public String getValue() {
        return value;
    }
}
