package core.parsers.params;

import core.commands.MyCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DisabledCommandParameters extends CommandParameters {

    private final MyCommand<?> command;
    private final long guildId;
    private final Long channelId;

    public DisabledCommandParameters(MessageReceivedEvent e, MyCommand<?> command, long guildId, Long channelId) {
        super(e);
        this.command = command;
        this.guildId = guildId;
        this.channelId = channelId;
    }

    public MyCommand<?> getCommand() {
        return command;
    }

    public long getGuildId() {
        return guildId;
    }

    public boolean isOnlyChannel() {
        return hasOptional("channel");
    }

    public boolean isAll() {
        return hasOptional("all");
    }

    public boolean isExceptThis() {
        return hasOptional("exceptthis");
    }

    public Long getChannelId() {
        return channelId;
    }
}
