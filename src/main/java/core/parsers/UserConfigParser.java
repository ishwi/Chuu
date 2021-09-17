package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.UserConfigParameters;
import core.parsers.params.UserConfigType;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.RemainingImagesMode;
import dao.entities.WhoKnowsMode;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserConfigParser extends DaoParser<UserConfigParameters> implements Generable<UserConfigParameters> {
    private static final Set<String> multipleWordsConfigs = Stream.of(UserConfigType.NP, UserConfigType.COLOR, UserConfigType.CHART_OPTIONS).map(UserConfigType::getCommandName).collect(Collectors.toSet());

    public UserConfigParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    public UserConfigParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        String subcommandName = e.getSubcommandName();
        String[] words;
        if (subcommandName.equals("list")) {
            words = new String[]{};
        } else if (subcommandName.equals("size")) {
            words = new String[]{subcommandName, e.getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.joining("x"))};
        } else {
            words = Stream.concat(Stream.of(subcommandName), e.getOptions().stream().map(OptionMapping::getAsString)).toArray(String[]::new);
        }
        return parseLogic(ctx, words);
    }

    @Override
    protected UserConfigParameters parseLogic(Context e, String[] words) {
        char prefix = CommandUtil.getMessagePrefix(e);
        if (words.length == 1) {
            String line = Arrays.stream(UserConfigType.values()).filter(x -> x.getCommandName().equalsIgnoreCase(words[0])).map(x ->
                    String.format("\t**%s** ➜ %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
            if (line.isBlank()) {
                line = Arrays.stream(UserConfigType.values()).map(UserConfigType::getCommandName).collect(Collectors.joining(", "));
                sendError(words[0] + " is not a valid configuration, use one of the following:\n\t" + line, e);
            } else {
                e.sendMessage(line).queue();
            }
            return null;
        }
        if ((words.length == 0) || (words.length > 2 && !multipleWordsConfigs.contains(words[0]))) {
            String list = UserConfigType.list(dao, e.getAuthor().getIdLong());
            sendError("The config format must be the following: **`Command`**  **`Value`**\n do " + prefix + "help config for more info.\nCurrent Values:\n" + list, e);
            return null;
        }
        String command = words[0];
        StringBuilder argsB = new StringBuilder();
        for (int i = 1; i < words.length; i++) {
            argsB.append(words[i]).append(" ");
        }
        String args = argsB.toString().trim();

        UserConfigType userConfigType = UserConfigType.get(command);
        if (userConfigType == null) {
            String line = Arrays.stream(UserConfigType.values()).map(UserConfigType::getCommandName).collect(Collectors.joining(", "));
            sendError(command + " is not a valid configuration, use one of the following:\n\t" + line, e);
            return null;
        }
        if (!userConfigType.getParser().test(args)) {
            sendError(String.format("%s is not a valid value for %s", args, WordUtils.capitalizeFully(command)), e);
            return null;
        }
        return new UserConfigParameters(e, userConfigType, args);


    }

    @Override
    public List<Explanation> getUsages() {
        String line = Arrays.stream(UserConfigType.values()).map(x -> String.format("\t**%s** ➜ %s", x.getCommandName(), x.getExplanation())).collect(Collectors.joining("\n"));
        String usage = "Possible values:\n" + line;
        return Collections.singletonList(() -> new ExplanationLineType("Command with argument", usage, OptionType.STRING));
    }

    @Override
    protected void setUpErrorMessages() {
        //overriding
    }

    @Override
    public CommandData generateCommandData(MyCommand<?> myCommand) {
        CommandData commandData = new CommandData("user-config", "user configuration");
        SubcommandData user = new SubcommandData("list", "List all the user configs");
        commandData.addSubcommands(user);
        Command.Choice clear = new Command.Choice("default", "clear");

        EnumSet<UserConfigType> userConfigTypes = EnumSet.allOf(UserConfigType.class);
        for (UserConfigType userConfigType : userConfigTypes) {
            SubcommandData data = new SubcommandData(userConfigType.getCommandName(), StringUtils.abbreviate(userConfigType.getExplanation(), 100));
            switch (userConfigType) {
                case CHART_MODE -> {
                    OptionData mode = new OptionData(OptionType.STRING, "chart-mode", StringUtils.abbreviate(userConfigType.getExplanation(), 100), true);
                    for (ChartMode value : ChartMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.toString());
                    }
                    mode.addChoices(clear);
                    data.addOptions(mode);
                }
                case WHOKNOWS_MODE -> {
                    OptionData mode = new OptionData(OptionType.STRING, "whoknows-mode", StringUtils.abbreviate(userConfigType.getExplanation(), 100), true);
                    for (WhoKnowsMode value : WhoKnowsMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.name());
                    }
                    mode.addChoices(clear);

                    data.addOptions(mode);
                }
                case COLOR -> {
                    SubcommandGroupData group = new SubcommandGroupData(userConfigType.getCommandName(), StringUtils.abbreviate(userConfigType.getExplanation(), 100));
                    OptionData mode = new OptionData(OptionType.STRING, "mode", "mode to select", true);
                    mode.addChoice("Random", "random");
                    mode.addChoice("Role", "role");
                    mode.addChoice("Colours", "colours");
                    mode.addChoices(clear);

                    OptionData chosen = new OptionData(OptionType.STRING, "colours-select", "Fill with a list of colours. Only if you have chosen colour mode");
                    data.addOptions(mode, chosen);
                }
                case PRIVATE_UPDATE, NOTIFY_IMAGE, NOTIFY_RATING, PRIVATE_LASTFM, SHOW_BOTTED, SCROBBLING, OWN_TAGS -> {
                    OptionData mode = new OptionData(OptionType.BOOLEAN, userConfigType.getCommandName(), StringUtils.abbreviate(userConfigType.getExplanation(), 95), true);
                    data.addOptions(mode);
                }
                case REMAINING_MODE -> {
                    OptionData mode = new OptionData(OptionType.STRING, "remaining-mode", StringUtils.abbreviate(userConfigType.getExplanation(), 100), true);
                    for (RemainingImagesMode value : RemainingImagesMode.values()) {
                        mode.addChoice(WordUtils.capitalizeFully(value.toString()), value.name());
                    }
                    mode.addChoices(clear);

                    data.addOptions(mode);
                }
                case CHART_SIZE -> {
                    OptionData columns = new OptionData(OptionType.NUMBER, "columns", "number of columns for the chart", true);
                    OptionData rows = new OptionData(OptionType.NUMBER, "rows", "number of rows for the chart", true);
                    for (int i = 1; i <= 7; i++) {
                        columns.addChoice(String.valueOf(i), (double) i);
                        rows.addChoice(String.valueOf(i), (double) i);
                    }
                    data.addOptions(columns, rows);
                }
                case ARTIST_THRESHOLD -> {
                    OptionData artistThreshold = new OptionData(OptionType.INTEGER, "artist-threshold", userConfigType.getExplanation(), true);
                    data.addOptions(artistThreshold);
                }
                case CHART_OPTIONS -> {
                    OptionData chartOptions = new OptionData(OptionType.STRING, "chart-options", StringUtils.abbreviate(userConfigType.getExplanation(), 100), true);
                    data.addOptions(chartOptions);
                }
                case TIMEZONE -> {
                }
            }
                commandData.addSubcommands(data);
        }
        return commandData;
    }
}
