package core.commands;

import core.Chuu;
import core.exceptions.LastFmException;
import core.parsers.DisabledCommandParser;
import core.parsers.Parser;
import core.parsers.params.DisabledCommandParameters;
import core.services.MessageDisablingService;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
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
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<DisabledCommandParameters> initParser() {
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
    void onCommand(MessageReceivedEvent e, @NotNull DisabledCommandParameters params) throws LastFmException, InstanceNotFoundException {

        MessageDisablingService messageDisablingService = Chuu.getMessageDisablingService();


        List<MyCommand<?>> commandsToAllow;
        if (params.hasOptional("all")) {
            commandsToAllow = e.getJDA().getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?> && !(x instanceof DisabledCommand)).map(x -> (MyCommand<?>) x).collect(Collectors.toList());
        } else if (params.hasOptional("category")) {
            commandsToAllow = e.getJDA().getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?> && !(x instanceof DisabledCommand)).map(x -> (MyCommand<?>) x).
                    filter(x -> x.getCategory().equals(params.getCommand().getCategory())).collect(Collectors.toList());
        } else {
            commandsToAllow = new ArrayList<>(Collections.singletonList(params.getCommand()));
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
            if (params.isExceptThis()) {
                messageDisablingService.toggleCommandDisabledness(command, params.getGuildId(), messageAllowed, getService());
                messageDisablingService.toggleCommandChannelDisabledness(command, params.getGuildId(), params.getChannelId(), !messageAllowed, getService());
            } else if (params.isOnlyChannel()) {
                messageDisablingService.toggleCommandChannelDisabledness(command, params.getGuildId(), params.getChannelId(), messageAllowed, getService());
            } else {
                messageDisablingService.toggleCommandDisabledness(command, params.getGuildId(), messageAllowed, getService());
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
