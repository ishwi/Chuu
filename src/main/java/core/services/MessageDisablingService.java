package core.services;

import core.commands.Context;
import core.commands.abstracts.MyCommand;
import core.commands.utils.ListUtils;
import dao.ChuuService;
import dao.entities.ChannelMapping;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageDisablingService {
    public final Map<Long, List<MyCommand<?>>> disabledServersMap = new HashMap<>();
    public final Map<ChannelMapping, List<MyCommand<?>>> disabledChannelsMap = new HashMap<>();
    public final Map<ChannelMapping, List<MyCommand<?>>> enabledChannelsMap = new HashMap<>();
    public final Set<Long> dontRespondOnErrorSet = new HashSet<>();

    public MessageDisablingService() {
    }

    public MessageDisablingService(JDA jda, ChuuService dao) {
        Map<String, MyCommand<?>> commandsByName = jda.getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?>).map(x -> (MyCommand<?>) x).collect(Collectors.toMap(MyCommand::getName, x -> x));
        Map<Long, List<String>> serverDisables = dao.initServerCommandStatuses();
        serverDisables.forEach((key, disabled) -> {
            for (String disable : disabled) {
                MyCommand<?> commandDisabled = commandsByName.get(disable);
                if (commandDisabled != null) {
                    disabledServersMap.computeIfAbsent(key, k -> new ArrayList<>()).add(commandDisabled);
                } else {
                    dao.deleteServerCommandStatus(key, disable);
                }
            }

        });
        var channelDisables = dao.initServerChannelsCommandStatuses(false);
        channelDisables.forEach((k, v) -> {
            for (String value : v) {
                MyCommand<?> commandDisabled = commandsByName.get(value);
                if (commandDisabled != null) {
                    disabledChannelsMap.computeIfAbsent(k, _k -> new ArrayList<>()).add(commandDisabled);
                } else {
                    dao.deleteChannelCommandStatus(k.guildId(), k.channelId(), value);
                }
            }

        });

        dontRespondOnErrorSet.addAll(dao.getServersDontRespondOnErrros());

        var channelEnables = dao.initServerChannelsCommandStatuses(true);
        channelEnables.forEach((k, v) -> {
            for (String val : v) {
                MyCommand<?> value = commandsByName.get(val);
                if (value != null) {
                    enabledChannelsMap.computeIfAbsent(k, _k -> new ArrayList<>()).add(value);
                } else {
                    dao.deleteChannelCommandStatus(k.guildId(), k.channelId(), val);
                }
            }

        });

    }

    public void toggleCommandChannelDisabledness(MyCommand<?> myCommand, long guildId, long channelId, boolean expectedResult, ChuuService service) {
        var channel = new ChannelMapping(guildId, channelId);
        boolean serverSet = ListUtils.hasMapping(disabledServersMap, guildId, myCommand);
        if (expectedResult) {
            disabledServersMap.compute(guildId, ListUtils.computeRemoval(myCommand));
            service.deleteChannelCommandStatus(guildId, channelId, myCommand.getName());
            if (serverSet) {
                enabledChannelsMap.computeIfAbsent(channel, k -> new ArrayList<>()).add(myCommand);
                service.insertChannelCommandStatus(guildId, channelId, myCommand.getName(), true);

            }  //Do Nothing

            // If this command was disabled server wide
        } else {
            enabledChannelsMap.compute(channel, ListUtils.computeRemoval(myCommand));

            service.deleteChannelCommandStatus(guildId, channelId, myCommand.getName());
            if (!serverSet) {
                disabledChannelsMap.computeIfAbsent(channel, k -> new ArrayList<>()).add(myCommand);
                service.insertChannelCommandStatus(guildId, channelId, myCommand.getName(), false);

            }
        }
    }

    public void setDontRespondOnError(boolean value, long guildId) {
        if (value) {
            dontRespondOnErrorSet.add(guildId);
        } else {
            dontRespondOnErrorSet.remove(guildId);
        }
    }

    public boolean isMessageAllowed(MyCommand<?> command, Context e) {
        if (!e.isFromGuild()) {
            return true;
        }
        long guildId = e.getGuild().getIdLong();
        long channelId = e.getChannel().getIdLong();
        var of = new ChannelMapping(guildId, channelId);
        return (!(disabledServersMap.getOrDefault(guildId, Collections.emptyList()).contains(command) || disabledChannelsMap.getOrDefault(of, Collections.emptyList()).contains(command)))
               || enabledChannelsMap.getOrDefault(of, Collections.emptyList()).contains(command);
    }

    //Returns if its disabled or enabled now
    public void toggleCommandDisabledness(MyCommand<?> myCommand, long guildId, boolean expectedResult, ChuuService service) {
        if (expectedResult) {
            disabledServersMap.compute(guildId, ListUtils.computeRemoval(myCommand));
            service.deleteServerCommandStatus(guildId, myCommand.getName());

            Set<Long> ids = disabledChannelsMap.keySet().stream().filter(myCommands -> myCommands.guildId() == guildId).map(ChannelMapping::channelId).collect(Collectors.toSet());
            disabledChannelsMap.entrySet().removeIf(x -> {
                boolean b = guildId == x.getKey().guildId();
                if (b) {
                    x.getValue().remove(myCommand);
                    return x.getValue().isEmpty();
                }
                return false;
            });
            ids.forEach(y -> service.deleteChannelCommandStatus(guildId, y, myCommand.getName()));
            service.deleteServerCommandStatus(guildId, myCommand.getName());

        } else {
            disabledServersMap.computeIfAbsent(guildId, k -> new ArrayList<>()).add(myCommand);
            service.insertServerDisabled(guildId, myCommand.getName());
        }
    }

    public boolean doResponse(Context e) {
        if (!e.isFromGuild()) {
            return true;
        }
        return !dontRespondOnErrorSet.contains(e.getGuild().getIdLong());
    }
}
