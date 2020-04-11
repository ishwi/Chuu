package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GuildConfigParams extends CommandParameters {
    private final GuildConfigType config;
    private final String value;

    public GuildConfigParams(MessageReceivedEvent e, GuildConfigType guildConfig, String value) {
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