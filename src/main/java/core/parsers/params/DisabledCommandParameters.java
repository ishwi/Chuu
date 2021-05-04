package core.parsers.params;

import core.commands.Context;
import core.commands.abstracts.MyCommand;

public class DisabledCommandParameters extends CommandParameters {

    private final MyCommand<?> command;
    private final long guildId;
    private final Long channelId;

    public DisabledCommandParameters(Context e, MyCommand<?> command, long guildId, Long channelId) {
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
