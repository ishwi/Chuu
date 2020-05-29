package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import core.parsers.params.UserConfigParameters;
import core.parsers.params.UserConfigType;
import dao.ChuuService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UserConfigParser extends DaoParser<UserConfigParameters> {
    public UserConfigParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected UserConfigParameters parseLogic(MessageReceivedEvent e, String[] words) {
        String prefix = e.getMessage().getContentRaw().substring(0, 1);
        if (words.length == 1) {
            String collect = Arrays.stream(UserConfigType.values()).filter(x -> x.getCommandName().equalsIgnoreCase(words[0])).map(x ->
                    String.format("\t**%s** -> %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
            if (collect.isBlank()) {
                collect = Arrays.stream(UserConfigType.values()).map(UserConfigType::getCommandName).collect(Collectors.joining(", "));
                sendError(words[0] + " is not a valid configuration, use one of the following:\n\t" + collect, e);
            } else {
                e.getChannel().sendMessage(collect).queue();
            }
            return null;
        }
        if (words.length != 2) {
            String list = UserConfigType.list(dao, e.getAuthor().getIdLong());
            sendError("The config format must be the following: **`Command`**  **`Value`**\n do " + prefix + "help config for more info.\nCurrent Values:\n" + list, e);
            return null;
        }
        String command = words[0];
        String args = words[1];

        UserConfigType userConfigType = UserConfigType.get(command);
        if (userConfigType == null) {
            String collect = Arrays.stream(UserConfigType.values()).map(UserConfigType::getCommandName).collect(Collectors.joining(", "));
            sendError(command + " is not a valid configuration, use one of the following:\n\t" + collect, e);
            return null;
        }
        if (!userConfigType.getParser().test(args)) {
            sendError(String.format("%s is not a valid value for %s", args, command.toUpperCase()), e);
            return null;
        }
        return new UserConfigParameters(e, userConfigType, args);


    }

    @Override
    public String getUsageLogic(String commandName) {
        String collect = Arrays.stream(UserConfigType.values()).map(x -> String.format("\t**%s** -> %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
        return "**" + commandName + " *command* *argument* ** \n" +
                "Possible Values:\n" + collect;
    }

    @Override
    protected void setUpErrorMessages() {
        //overriding
    }
}
