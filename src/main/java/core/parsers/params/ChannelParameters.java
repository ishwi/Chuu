package core.parsers.params;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChannelParameters extends CommandParameters {
    private final GuildChannel guildChannel;

    public ChannelParameters(MessageReceivedEvent e, GuildChannel guildChannel) {
        super(e);
        this.guildChannel = guildChannel;
    }

    public GuildChannel getGuildChannel() {
        return guildChannel;
    }
}
