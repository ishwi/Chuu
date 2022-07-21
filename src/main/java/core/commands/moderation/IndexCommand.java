package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.Command;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Command(aliases = "serverset",
        category = CommandCategory.CONFIGURATION,
        description = "Manually sets the user that have registered in other servers. This should only be used in case you have been using the bot for a while and there a lot of users which haven't set their accounts",
        name = "Server Set")
public class IndexCommand extends ConcurrentCommand<CommandParameters> {
    public IndexCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    protected EnumSet<Permission> initRequiredPerms() {
        return EnumSet.of(Permission.MESSAGE_MANAGE);
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Manually sets the user that have registered in other servers. This should only be used in case you have been using the bot for a while and there a lot of users which haven't set their accounts  ";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverset");
    }

    @Override
    public String getName() {
        return "Server Set";
    }


    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        if (CommandUtil.notEnoughPerms(e)) {
            sendMessageQueue(e, CommandUtil.notEnoughPermsTemplate() + "re-index the whole serverlist");
            return;
        }
        DefaultMemberPermissions perms = DefaultMemberPermissions.enabledFor(Permission.MANAGE_PERMISSIONS);

        Set<Long> allBot = db.getAllALL().stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());
        Set<Long> thisServer = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());
        Set<Long> serverBlocked = db.getServerBlocked(e.getGuild().getIdLong());
        e.getGuild().loadMembers().onSuccess(_members -> {
            var members = _members.stream().filter(x -> x.getUser().getIdLong() != x.getJDA().getSelfUser().getIdLong()).map(x -> x.getUser().getIdLong()).collect(Collectors.toSet());
            List<Long> toInsert = members.stream().filter(x -> allBot.contains(x) && !thisServer.contains(x) && !serverBlocked.contains(x)).toList();
            toInsert.forEach(x -> db.addGuildUser(x, e.getGuild().getIdLong()));
            List<Long> notOnServer = thisServer.stream().filter(x -> !members.contains(x)).toList();
            if (toInsert.isEmpty()) {
                sendMessageQueue(e, "Didn't have anyone to add");
            } else {
                sendMessageQueue(e, String.format("Successfully added %s %s to this server", toInsert.size(), CommandUtil.singlePlural(toInsert.size(), "member", "members")));
            }
            for (Long id : toInsert) {
                Chuu.refreshCache(id);
            }
            if (!notOnServer.isEmpty()) {
                notOnServer.forEach(x -> db.removeUserFromOneGuildConsequent(x, e.getGuild().getIdLong()));
                sendMessageQueue(e, String.format("Removed %s %s that no longer were in this server", notOnServer.size(), CommandUtil.singlePlural(notOnServer.size(), "member", "members")));
            }
        });
    }
}
