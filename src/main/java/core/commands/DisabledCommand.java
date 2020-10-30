package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.DisabledCommandParser;
import core.parsers.Parser;
import core.parsers.params.DisabledCommandParameters;
import core.services.MessageDisablingService;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DisabledCommand extends ConcurrentCommand<DisabledCommandParameters> {
    public DisabledCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<DisabledCommandParameters> getParser() {
        return new DisabledCommandParser();
    }

    @Override
    public String getDescription() {
        return "Disable and re enable all the commands of the bot in a specific channel or in the whole server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("disable", "enable", "toggle");
    }

    @Override
    public String getName() {
        return "Disable";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        DisabledCommandParameters parse = parser.parse(e);
        MessageDisablingService messageDisablingService = Chuu.getMessageDisablingService();
        if (parse == null) {
            return;
        }

        List<MyCommand<?>> commandsToAllow;
        if (parse.hasOptional("all")) {
            commandsToAllow = e.getJDA().getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?> && !(x instanceof DisabledCommand)).map(x -> (MyCommand<?>) x).collect(Collectors.toList());
        } else if (parse.hasOptional("category")) {
            commandsToAllow = e.getJDA().getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?> && !(x instanceof DisabledCommand)).map(x -> (MyCommand<?>) x).
                    filter(x -> x.getCategory().equals(parse.getCommand().getCategory())).collect(Collectors.toList());
        } else {
            commandsToAllow = new ArrayList<>(Collections.singletonList(parse.getCommand()));
        }
        // Wont accept this command
        commandsToAllow.removeIf(x -> x.getName().equals(this.getName()));
        StringBuilder s = new StringBuilder();
        String substring = e.getMessage().getContentRaw().substring(1);

        boolean enable = substring.startsWith("enable");
        boolean toggl = substring.startsWith("toggle");
        Predicate<Boolean> transformation = (b) -> {
            if (toggl) {
                return !b;
            } else
                return enable;
        };


        Map<Boolean, List<MyCommand<?>>> collect = commandsToAllow.stream().collect(Collectors.partitioningBy(x -> transformation.test(messageDisablingService.isMessageAllowed(x, e))));
        List<MyCommand<?>> previouslyAllowedCommands = collect.get(true);
        for (MyCommand<?> command : commandsToAllow) {
            boolean messageAllowed = previouslyAllowedCommands.contains(command);
            if (parse.isExceptThis()) {
                messageDisablingService.toggleCommandDisabledness(command, parse.getGuildId(), messageAllowed, getService());
                messageDisablingService.toggleCommandChannelDisabledness(command, parse.getGuildId(), parse.getChannelId(), !messageAllowed, getService());
            } else if (parse.isOnlyChannel()) {
                messageDisablingService.toggleCommandChannelDisabledness(command, parse.getGuildId(), parse.getChannelId(), messageAllowed, getService());
            } else {
                messageDisablingService.toggleCommandDisabledness(command, parse.getGuildId(), messageAllowed, getService());
            }
        }
        Character prefix = Chuu.getCorrespondingPrefix(e);
        String collect1 = collect.entrySet().stream()
                .map(x -> {
                    String commands = x.getValue().stream()
                            .map(y -> prefix + y.getAliases().get(0))
                            .collect(Collectors.joining(", "));
                    if (commands.isBlank()) return "";
                    else
                        return commands + (x.getValue().size() > 1 ? " are now " : " is now ")
                                + (x.getKey() ? "enabled." : "disabled.") + "\n";
                }).collect(Collectors.joining(""));
        sendMessageQueue(e, collect1);
    }
}
