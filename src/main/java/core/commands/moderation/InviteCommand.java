package core.commands.moderation;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class InviteCommand extends ConcurrentCommand<CommandParameters> {
    private static final long PERMISSIONS = 387136;

    public InviteCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Invite the bot to other servers!";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("invite");
    }

    @Override
    public String getName() {
        return "Invite";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        EnumSet<Permission> permissions = Permission.getPermissions(PERMISSIONS);
        String inviteUrl = e.getJDA().getInviteUrl(permissions);
        sendMessageQueue(e, "Using the following link you can invite me to your server:\n" + inviteUrl);
    }
}
