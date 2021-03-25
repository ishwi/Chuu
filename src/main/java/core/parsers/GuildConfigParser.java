package core.parsers;

import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import dao.ChuuService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuildConfigParser extends DaoParser<GuildConfigParams> {
    private static final Set<String> multipleWordsConfigs = Stream.of(GuildConfigType.NP, GuildConfigType.COLOR).map(GuildConfigType::getCommandName).collect(Collectors.toSet());

    public GuildConfigParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    protected GuildConfigParams parseLogic(MessageReceivedEvent e, String[] words) {

        if (e.getMember() == null || !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            sendError("Only admins can modify the server configuration", e);
            return null;
        }
        if (words.length == 1) {
            String line = Arrays.stream(GuildConfigType.values()).filter(x -> x.getCommandName().equalsIgnoreCase(words[0])).map(x ->
                    String.format("\t**%s** -> %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
            if (line.isBlank()) {
                line = Arrays.stream(GuildConfigType.values()).map(GuildConfigType::getCommandName).collect(Collectors.joining(", "));
                sendError(words[0] + " is not a valid configuration, use one of the following:\n\t" + line, e);
            } else {
                e.getChannel().sendMessage(line).queue();
            }
            return null;
        }
        if ((words.length == 0) || (words.length > 2 && !multipleWordsConfigs.contains(words[0]))) {
            String prefix = e.getMessage().getContentRaw().substring(0, 1);
            String list = GuildConfigType.list(dao, e.getGuild().getIdLong());
            sendError("The config format must be the following: **`Command`**  **`Value`**\n do " + prefix + "help sconfig for more info.\nCurrent Values:\n" + list, e);
            return null;
        }
        String command = words[0];
        StringBuilder argsB = new StringBuilder();
        for (int i = 1; i < words.length; i++) {
            argsB.append(words[i]).append(" ");
        }
        String args = argsB.toString().trim();

        GuildConfigType guildConfigType = GuildConfigType.get(command);
        if (guildConfigType == null) {
            String line = Arrays.stream(GuildConfigType.values()).map(GuildConfigType::getCommandName).collect(Collectors.joining(", "));
            sendError(command + " is not a valid configuration, use one of the following:\n\t" + line, e);
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
        String line = Arrays.stream(GuildConfigType.values()).map(x -> String.format("\t**%s** -> %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
        return "**" + commandName + " *command* *argument* ** \n" +
                "Possible Values:\n" + line;
    }

    @Override
    protected void setUpErrorMessages() {
        //overriding
    }
}
