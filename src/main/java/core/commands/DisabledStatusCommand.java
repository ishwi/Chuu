package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DisabledStatusCommand extends ConcurrentCommand<CommandParameters> {
    public DisabledStatusCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "A list of all the disabled commands in the server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("disabled", "commandstatus", "enabled", "toggled");
    }

    @Override
    public String getName() {
        return "Command Statuses";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        MultiValuedMap<Pair<Long, Long>, MyCommand<?>> disabledChannelsMap = Chuu.disabledChannelsMap;
        MultiValuedMap<Pair<Long, Long>, MyCommand<?>> enabledChannelsMap = Chuu.enabledChannelsMap;
        MultiValuedMap<Long, MyCommand<?>> disabledServersMap = Chuu.disabledServersMap;
        Map<Long, GuildChannel> channelMap = e.getGuild().getChannels().stream().collect(Collectors.toMap(ISnowflake::getIdLong, x ->
                x));
        List<? extends MyCommand<?>>
                disabledServerCommands = disabledServersMap.entries().stream().filter(x -> x.getKey().equals(e.getGuild().getIdLong())).map(Map.Entry::getValue).collect(Collectors.toList());
        Map<GuildChannel, List<MyCommand<?>>> channelSpecificDisables = disabledChannelsMap.entries().stream()
                .filter(x -> x.getKey().getLeft().equals(e.getGuild().getIdLong()))
                .filter(x -> !disabledServerCommands.contains(x.getValue()))
                .collect(Collectors.groupingBy(x -> channelMap.get(x.getKey().getRight()),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        Map<GuildChannel, List<MyCommand<?>>> channelSpecificEnabled = enabledChannelsMap.entries().stream()
                .filter(x -> x.getKey().getLeft().equals(e.getGuild().getIdLong()))
                .filter(x -> disabledServerCommands.contains(x.getValue()))
                .collect(Collectors.groupingBy(x -> channelMap.get(x.getKey().getRight()),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        Map<GuildChannel, StringBuilder> fieldBuilder = new HashMap<>();
        String globalDisabled = disabledServerCommands.stream().map(x -> x.getAliases().get(0)).collect(Collectors.joining(", "));
        if (!globalDisabled.isBlank()) {
            embedBuilder.addField("**Commands disabled in the whole server**", globalDisabled, false);
        }
        for (Map.Entry<GuildChannel, List<MyCommand<?>>> guildChannelListEntry : channelSpecificDisables.entrySet()) {
            GuildChannel key = guildChannelListEntry.getKey();
            StringBuilder stringBuilder = fieldBuilder.get(key);
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
                fieldBuilder.put(key, stringBuilder);
            }
            stringBuilder.append("Disabled:\t");
            stringBuilder.append(guildChannelListEntry.getValue().stream().map(x -> x.getAliases().get(0)).collect(Collectors.joining(", ")));
            stringBuilder.append("\n");

        }
        for (Map.Entry<GuildChannel, List<MyCommand<?>>> guildChannelListEntry : channelSpecificEnabled.entrySet()) {
            GuildChannel key = guildChannelListEntry.getKey();
            StringBuilder stringBuilder = fieldBuilder.get(key);
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
                fieldBuilder.put(key, stringBuilder);
            }
            stringBuilder.append("Enabled:\t");
            stringBuilder.append(guildChannelListEntry.getValue().stream().map(x -> x.getAliases().get(0)).collect(Collectors.joining(", ")));
            stringBuilder.append("\n");
        }
        fieldBuilder.forEach((x, y) -> embedBuilder.addField("**Channel " + x.getName() + "**", y.toString(), false));

        if (embedBuilder.getFields().isEmpty()) {
            sendMessageQueue(e, " This server has not disabled any command");
            return;
        }

        embedBuilder.setDescription(a).setTitle(CommandUtil.cleanMarkdownCharacter(e.getGuild().getName()) + "'s commands status")
                .setThumbnail(e.getGuild().getIconUrl());
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
    }
}
