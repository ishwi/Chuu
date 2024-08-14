package core.commands.moderation;

import core.Chuu;
import core.apis.lyrics.TextSplitter;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.abstracts.MyCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.services.MessageDisablingService;
import core.util.ServiceView;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DisabledStatusCommand extends ConcurrentCommand<CommandParameters> {
    public DisabledStatusCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
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
    public void onCommand(Context e, @NotNull CommandParameters params) {
        MessageDisablingService messageDisablingService = Chuu.getMessageDisablingService();
        var disabledChannelsMap = messageDisablingService.disabledChannelsMap;
        var enabledChannelsMap = messageDisablingService.enabledChannelsMap;
        var disabledServersMap = messageDisablingService.disabledServersMap;
        Map<Long, GuildChannel> channelMap = e.getGuild().getChannels().stream().collect(Collectors.toMap(ISnowflake::getIdLong, x ->
                x));
        List<? extends MyCommand<?>>
                disabledServerCommands = disabledServersMap.entrySet().stream()
                .filter(x -> x.getKey().equals(e.getGuild().getIdLong()))
                .flatMap(z -> z.getValue().stream())
                .toList();
        Map<GuildChannel, List<MyCommand<?>>> channelSpecificDisables = disabledChannelsMap.entrySet()
                .stream()
                .filter(x -> e.getGuild().getIdLong() == x.getKey().guildId())
                .collect(Collectors.groupingBy(x -> channelMap.get(x.getKey().channelId()),
                        Collectors.flatMapping(x -> x.getValue().stream(),
                                Collectors.filtering(z ->
                                                !disabledServerCommands.contains(z),
                                        Collectors.toList()))));

        Map<GuildChannel, List<MyCommand<?>>> channelSpecificEnabled = enabledChannelsMap.entrySet().stream()
                .filter(x -> x.getKey().guildId() == e.getGuild().getIdLong())
                .collect(Collectors.groupingBy(x -> channelMap.get(x.getKey().channelId()),
                        Collectors.flatMapping(x -> x.getValue().stream(),
                                Collectors.filtering(disabledServerCommands::contains,
                                        Collectors.toList()))));

        channelSpecificDisables.entrySet().removeIf(z -> z.getValue().isEmpty());
        channelSpecificEnabled.entrySet().removeIf(z -> z.getValue().isEmpty());

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        Map<GuildChannel, StringBuilder> fieldBuilder = new HashMap<>();
        String globalDisabled = disabledServerCommands.stream().map(x -> x.getAliases().get(0)).collect(Collectors.joining(", "));
        if (!globalDisabled.isBlank()) {
            if (globalDisabled.length() > 1024) {
                List<String> split = TextSplitter.split(globalDisabled, 1020, ", ");
                embedBuilder.addField("**Commands disabled in the whole server**", split.get(0), false);
                for (int i = 1; i < split.size(); i++) {
                    embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, split.get(i), false);
                }
            } else {
                embedBuilder.addField("**...**", globalDisabled, false);
            }
        }
        for (Map.Entry<GuildChannel, List<MyCommand<?>>> guildChannelListEntry : channelSpecificDisables.entrySet()) {
            GuildChannel key = guildChannelListEntry.getKey();
            StringBuilder stringBuilder = fieldBuilder.get(key);
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
                fieldBuilder.put(key, stringBuilder);
            }
            stringBuilder.append("Disabled:\t")
                    .append(guildChannelListEntry.getValue().stream().map(x -> x.getAliases().get(0)).collect(Collectors.joining(", ")))
                    .append("\n");

        }
        for (Map.Entry<GuildChannel, List<MyCommand<?>>> guildChannelListEntry : channelSpecificEnabled.entrySet()) {
            GuildChannel key = guildChannelListEntry.getKey();
            StringBuilder stringBuilder = fieldBuilder.get(key);
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
                fieldBuilder.put(key, stringBuilder);
            }
            stringBuilder.append("Enabled:\t")
                    .append(guildChannelListEntry.getValue().stream().map(x -> x.getAliases().get(0)).collect(Collectors.joining(", ")))
                    .append("\n");
        }
        fieldBuilder.forEach((x, y) -> {
            String value = y.toString();
            if (value.length() > 1024) {
                List<String> split = TextSplitter.split(value, 1020, ", ");
                embedBuilder.addField("**Channel " + x.getName() + "**", split.get(0), false);
                for (int i = 1; i < split.size(); i++) {
                    embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, split.get(i), false);
                }
            } else {
                embedBuilder.addField("**Channel " + x.getName() + "**", value, false);
            }
        });

        if (embedBuilder.getFields().isEmpty()) {
            sendMessageQueue(e, " This server has not disabled any command");
            return;
        }

        embedBuilder.setDescription(a).setTitle(CommandUtil.escapeMarkdown(e.getGuild().getName()) + "'s commands status")
                .setThumbnail(e.getGuild().getIconUrl());
        e.sendMessage(embedBuilder.build()).queue();
    }
}
