package core.parsers.params;

import core.commands.Context;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class ChannelParameters extends CommandParameters {
    private final GuildChannel guildChannel;

    public ChannelParameters(Context e, GuildChannel guildChannel) {
        super(e);
        this.guildChannel = guildChannel;
    }

    public GuildChannel getGuildChannel() {
        return guildChannel;
    }
}
