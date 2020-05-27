package core.parsers;

import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import core.parsers.params.UserConfigType;
import dao.ChuuService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GuildConfigParser extends DaoParser<GuildConfigParams> {
    public GuildConfigParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected GuildConfigParams parseLogic(MessageReceivedEvent e, String[] words) {

        if (e.getMember() == null || !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            sendError("Only admins can modify the server configuration", e);
            return null;
        }
        if (words.length != 2) {
            String prefix = e.getMessage().getContentRaw().substring(0, 1);
            String list = GuildConfigType.list(dao, e.getGuild().getIdLong());
            sendError("The config format must be the following: **`Command`**  **`Value`**\n do " + prefix + "help sconfig for more info.\nCurrent Values:\n" + list, e);
            return null;
        }
        String command = words[0];
        String args = words[1];

        GuildConfigType guildConfigType = GuildConfigType.get(command);
        if (guildConfigType == null) {
            String collect = Arrays.stream(GuildConfigType.values()).map(GuildConfigType::getCommandName).collect(Collectors.joining(", "));
            sendError(command + " is not a valid configuration, use one of the following:\n\t" + collect, e);
            return null;
        }
        if (!guildConfigType.getParser().test(args)) {
            sendError(String.format("%s is not a valid value for %s", args, command.toUpperCase()), e);
            return null;
        }
        return new GuildConfigParams(e, guildConfigType, args);


    }

    @Override
    public String getUsageLogic(String commandName) {
        String collect = Arrays.stream(GuildConfigType.values()).map(x -> String.format("\t**%s** -> %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
        return "**" + commandName + " *command* *argument* ** \n" +
                "Possible Values:\n" + collect;
    }

    @Override
    protected void setUpErrorMessages() {
        //overriding
    }
}
