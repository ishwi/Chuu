package core.commands;

import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IndexCommand extends ConcurrentCommand<CommandParameters> {
    public IndexCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
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
    void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            sendMessageQueue(e, "Only server mods can use this command");
            return;
        }
        Set<Long> allBot = getService().getAllALL().stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());
        Set<Long> thisServer = getService().getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toUnmodifiableSet());

        Set<Long> members = e.getGuild().getMembers().stream().map(x -> x.getUser().getIdLong()).collect(Collectors.toSet());
        List<Long> toInsert = members.stream().filter(x -> allBot.contains(x) && !thisServer.contains(x)).collect(Collectors.toList());
        toInsert.forEach(x -> getService().addGuildUser(x, e.getGuild().getIdLong()));
        if (toInsert.isEmpty()) {
            sendMessageQueue(e, "Didn't have anyone to add");
        } else {
            sendMessageQueue(e, String.format("Succesfully added %s %s to this server", toInsert.size(), CommandUtil.singlePlural(toInsert.size(), "member", "members")));
        }
    }
}
