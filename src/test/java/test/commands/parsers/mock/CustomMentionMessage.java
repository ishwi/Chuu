package test.commands.parsers.mock;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MessageMentionsImpl;

public class CustomMentionMessage extends MessageMentionsImpl {
    public CustomMentionMessage(JDAImpl impl, GuildImpl guild, String content) {
        super(impl, guild, content, false, DataArray.empty(), DataArray.empty());
    }

    @Override
    protected boolean isUserMentioned(IMentionable mentionable) {
        return false;
    }
}
