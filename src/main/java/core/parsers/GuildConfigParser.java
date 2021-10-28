package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuildConfigParser extends DaoParser<GuildConfigParams> implements Generable<GuildConfigParams> {
    private static final Set<String> multipleWordsConfigs = Stream.of(GuildConfigType.NP, GuildConfigType.COLOR).map(GuildConfigType::getCommandName).collect(Collectors.toSet());

    public GuildConfigParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    public GuildConfigParams parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        String subcommandName = e.getSubcommandName();
        String[] words;
        if (subcommandName.equals("list")) {
            words = new String[]{};
        } else {
            words = Stream.concat(Stream.of(subcommandName), e.getOptions().stream().map(OptionMapping::getAsString)).toArray(String[]::new);
        }
        return parseLogic(ctx, words);
    }

    @Override
    protected GuildConfigParams parseLogic(Context e, String[] words) {

        if (CommandUtil.notEnoughPerms(e)) {
            sendError(CommandUtil.notEnoughPermsTemplate() + "modify the server configuration", e);
            return null;
        }


        if (words.length == 1) {
            String line = Arrays.stream(GuildConfigType.values()).filter(x -> x.getCommandName().equalsIgnoreCase(words[0])).map(x ->
                    String.format("\t**%s** ➜ %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
            if (line.isBlank()) {
                line = Arrays.stream(GuildConfigType.values()).map(GuildConfigType::getCommandName).collect(Collectors.joining(", "));
                sendError(words[0] + " is not a valid configuration, use one of the following:\n\t" + line, e);
            } else {
                e.sendMessage(line).queue();
            }
            return null;
        }
        if ((words.length == 0) || (words.length > 2 && !multipleWordsConfigs.contains(words[0]))) {
            char prefix = CommandUtil.getMessagePrefix(e);
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
    public List<Explanation> getUsages() {
        String line = Arrays.stream(GuildConfigType.values()).map(x -> String.format("\t**%s** ➜ %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
        String usage = "Possible values:\n" + line;
        OptionData optionData = new OptionData(OptionType.STRING, "config", "Name of the config");
        for (GuildConfigType value : GuildConfigType.values()) {
            optionData.addChoice(value.getCommandName(), value.getCommandName());
        }
        return List.of(() -> new ExplanationLine("config-property", usage, optionData), () -> new ExplanationLineType("config-value", "", OptionType.STRING));
    }

    @Override
    protected void setUpErrorMessages() {
        //overriding
    }


    @Override
    public CommandData generateCommandData(MyCommand<?> myCommand) {
        CommandData commandData = new CommandData("server-config", "server configuration");
        SubcommandData user = new SubcommandData("list", "List all the server configs");
        commandData.addSubcommands(user);
        Command.Choice clear = new Command.Choice("Default", "clear");
        EnumSet<GuildConfigType> userConfigTypes = EnumSet.allOf(GuildConfigType.class);
        for (GuildConfigType guildConfigType : userConfigTypes) {
            SubcommandData data = new SubcommandData(guildConfigType.getCommandName(), StringUtils.abbreviate(guildConfigType.getExplanation(), 100));
            switch (guildConfigType) {
                case CHART_MODE -> {
                    OptionData mode = new OptionData(OptionType.STRING, "chart-mode", StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    for (ChartMode value : ChartMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.toString());
                    }
                    mode.addChoices(clear);
                    data.addOptions(mode);
                }
                case WHOKNOWS_MODE -> {
                    OptionData mode = new OptionData(OptionType.STRING, "whoknows-mode", StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    for (WhoKnowsMode value : WhoKnowsMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.name());
                    }
                    mode.addChoices(clear);
                    data.addOptions(mode);
                }
                case COLOR -> {
                    OptionData mode = new OptionData(OptionType.STRING, "mode", "mode to select", true);
                    mode.addChoice("Random", "random");
                    mode.addChoice("Role", "role");
                    mode.addChoice("Colours", "colours");
                    mode.addChoices(clear);
                    OptionData chosen = new OptionData(OptionType.STRING, "colours-select", "Fill with a list of colours. Only if you have chosen colour mode");
                    data.addOptions(mode, chosen);
                }
                case CROWNS_THRESHOLD -> {
                    OptionData artistThreshold = new OptionData(OptionType.INTEGER, "crowns-threshold", guildConfigType.getExplanation(), true);
                    data.addOptions(artistThreshold);
                }
                case OVERRIDE_NP_REACTIONS -> {
                    OptionData mode = new OptionData(OptionType.STRING, "override-np-override", StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    for (OverrideMode value : OverrideMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.name());
                    }
                    data.addOptions(mode);
                }
                case OVERRIDE_COLOR -> {
                    OptionData mode = new OptionData(OptionType.STRING, "override-color", StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    for (OverrideColorMode value : OverrideColorMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.name());
                    }
                    data.addOptions(mode);
                }

                case REMAINING_MODE -> {
                    OptionData mode = new OptionData(OptionType.STRING, "remaining-mode", StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    for (RemainingImagesMode value : RemainingImagesMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.name());
                    }
                    mode.addChoices(clear);
                    data.addOptions(mode);
                }

                case VOICE_ANNOUNCEMENT_CHANNEL -> {
                    OptionData mode = new OptionData(OptionType.CHANNEL, guildConfigType.getCommandName(), StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    data.addOptions(mode);
                }
                case CENSOR_CONVERS, SET_ON_JOIN, VOICE_ANNOUNCEMENT_ENABLED, SHOW_DISABLED_WARNING, DELETE_MESSAGE, ALLOW_NP_REACTIONS -> {
                    OptionData mode = new OptionData(OptionType.BOOLEAN, guildConfigType.getCommandName(), StringUtils.abbreviate(guildConfigType.getExplanation(), 100), true);
                    data.addOptions(mode);
                }
            }
            if (!data.getOptions().isEmpty()) {
                commandData.addSubcommands(data);
            }
        }
        return commandData;
    }
}
