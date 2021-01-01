package core.services;

import core.commands.abstracts.MyCommand;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageDisablingService {
    public final MultiValuedMap<Long, MyCommand<?>> disabledServersMap = new HashSetValuedHashMap<>();
    public final MultiValuedMap<Pair<Long, Long>, MyCommand<?>> disabledChannelsMap = new HashSetValuedHashMap<>();
    public final MultiValuedMap<Pair<Long, Long>, MyCommand<?>> enabledChannelsMap = new HashSetValuedHashMap<>();
    public final Set<Long> dontRespondOnErrorSet = new HashSet<>();

    public MessageDisablingService() {
    }

    public MessageDisablingService(ShardManager jda, ChuuService dao) {
        Map<String, MyCommand<?>> commandsByName = jda.getShards().get(0).getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?>).map(x -> (MyCommand<?>) x).collect(Collectors.toMap(MyCommand::getName, x -> x));
        MultiValuedMap<Long, String> serverDisables = dao.initServerCommandStatuses();
        serverDisables.entries().forEach(x -> {
            MyCommand<?> commandDisabled = commandsByName.get(x.getValue());
            if (commandDisabled != null) {
                disabledServersMap.put(x.getKey(), commandDisabled);
            } else {
                dao.deleteServerCommandStatus(x.getKey(), x.getValue());
            }
        });
        MultiValuedMap<Pair<Long, Long>, String> channelDisables = dao.initServerChannelsCommandStatuses(false);
        channelDisables.entries().forEach(x -> {
            MyCommand<?> commandDisabled = commandsByName.get(x.getValue());
            if (commandDisabled != null) {
                disabledChannelsMap.put(x.getKey(), commandDisabled);
            } else {
                dao.deleteChannelCommandStatus(x.getKey().getLeft(), x.getKey().getRight(), x.getValue());
            }
        });
        MultiValuedMap<Pair<Long, Long>, String> channelEnables = dao.initServerChannelsCommandStatuses(true);
        dontRespondOnErrorSet.addAll(dao.getServersDontRespondOnErrros());
        channelEnables.entries().forEach(x -> {
            MyCommand<?> value = commandsByName.get(x.getValue());
            if (value != null) {
                enabledChannelsMap.put(x.getKey(), value);
            } else {
                dao.deleteChannelCommandStatus(x.getKey().getLeft(), x.getKey().getRight(), x.getValue());
            }
        });

    }

    public void toggleCommandChannelDisabledness(MyCommand<?> myCommand, long guildId, long channelId, boolean expectedResult, ChuuService service) {
        Pair<Long, Long> channel = Pair.of(guildId, channelId);
        boolean serverSet = disabledServersMap.containsMapping(guildId, myCommand);
        if (expectedResult) {
            disabledServersMap.removeMapping(channel, myCommand);
            service.deleteChannelCommandStatus(guildId, channelId, myCommand.getName());
            if (serverSet) {
                enabledChannelsMap.put(channel, myCommand);
                service.insertChannelCommandStatus(guildId, channelId, myCommand.getName(), true);

            }  //Do Nothing

            // If this command was disabled server wide
        } else {
            enabledChannelsMap.removeMapping(channel, myCommand);
            service.deleteChannelCommandStatus(guildId, channelId, myCommand.getName());
            if (!serverSet) {
                disabledChannelsMap.put(channel, myCommand);
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

    public boolean isMessageAllowed(MyCommand<?> command, MessageReceivedEvent e) {
        if (!e.isFromGuild()) {
            return true;
        }
        long guildId = e.getGuild().getIdLong();
        long channelId = e.getChannel().getIdLong();
        return (!(disabledServersMap.get(guildId).contains(command) || disabledChannelsMap.get(Pair.of(guildId, channelId)).contains(command)))
                || enabledChannelsMap.get(Pair.of(guildId, channelId)).contains(command);
    }

    //Returns if its disabled or enabled now
    public void toggleCommandDisabledness(MyCommand<?> myCommand, long guildId, boolean expectedResult, ChuuService service) {
        if (expectedResult) {
            disabledServersMap.removeMapping(guildId, myCommand);
            service.deleteServerCommandStatus(guildId, myCommand.getName());

            Set<Long> collect = disabledChannelsMap.entries().stream().filter(x -> x.getKey().getLeft().equals(guildId)).map(x -> x.getKey().getRight()).collect(Collectors.toSet());
            disabledChannelsMap.entries().removeIf(x -> x.getKey().getLeft().equals(guildId));
            collect.forEach(y -> service.deleteChannelCommandStatus(guildId, y, myCommand.getName()));
            service.deleteServerCommandStatus(guildId, myCommand.getName());

        } else {
            disabledServersMap.put(guildId, myCommand);
            service.insertServerDisabled(guildId, myCommand.getName());
        }
    }

    public boolean doResponse(MessageReceivedEvent e) {
        if (!e.isFromGuild()) {
            return true;
        }
        return !dontRespondOnErrorSet.contains(e.getGuild().getIdLong());
    }
}
