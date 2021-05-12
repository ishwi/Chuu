package core.interactions;

import core.commands.abstracts.MyCommand;
import core.commands.charts.WastedAlbumChartCommand;
import core.commands.charts.WastedChartCommand;
import core.commands.charts.WastedTrackCommand;
import core.commands.moderation.EvalCommand;
import core.commands.moderation.MbidUpdatedCommand;
import core.commands.moderation.RefreshSlashCommand;
import core.commands.stats.*;
import core.commands.utils.CommandCategory;
import core.parsers.OptionalEntity;
import core.parsers.explanation.util.Explanation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class InteractionBuilder {
    private static final Set<CommandCategory> categorized = Set.of(
            CommandCategory.RYM,
            CommandCategory.BOT_INFO,
            CommandCategory.CROWNS,
            CommandCategory.UNIQUES,
            CommandCategory.SERVER_LEADERBOARDS,
            CommandCategory.BOT_STATS,
            CommandCategory.SERVER_STATS,
            CommandCategory.TRENDS,
            CommandCategory.STREAKS,
            CommandCategory.CONFIGURATION,
            CommandCategory.MODERATION,
            CommandCategory.STARTING,
            CommandCategory.MUSIC,
            CommandCategory.DISCOVERY
    );
    private static final Set<Class<? extends MyCommand<?>>> ignored = Set.of(EvalCommand.class, MbidUpdatedCommand.class, RefreshSlashCommand.class);

    private static final Set<Class<? extends MyCommand<?>>> timeGrouped =
            Set.of(WastedChartCommand.class,
                    WastedTrackCommand.class,
                    FirstArtistCommand.class,
                    FirstPlayedCommand.class,
                    LastPlayedArtistCommand.class,
                    LastPlayedCommand.class,
                    TimeSpentCommand.class,
                    WastedAlbumChartCommand.class,
                    WeeklyCommand.class,
                    DailyCommand.class);


    @CheckReturnValue
    public static CommandUpdateAction setGlobalCommands(JDA jda) {
        CommandUpdateAction commandUpdateAction = jda.updateCommands();
        return fillAction(jda, commandUpdateAction);
    }

    @CheckReturnValue
    public static CommandUpdateAction setServerCommand(Guild guild) {
        CommandUpdateAction commandUpdateAction = guild.updateCommands();
        return fillAction(guild.getJDA(), commandUpdateAction);
    }

    private static final Predicate<MyCommand<?>> test = t -> {
        try {
            t.getParser().parseSlashLogic(null);
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        } catch (Exception e) {
            return true;
        }
    };

    private static CommandUpdateAction fillAction(JDA jda, CommandUpdateAction commandUpdateAction) {
        List<? extends MyCommand<?>> myCommands = jda.getRegisteredListeners().stream()
                .filter(t -> t instanceof MyCommand<?>)
                .map(t -> (MyCommand<?>) t)
                .sorted(Comparator.comparingInt(t -> t.order))
                .filter(test)
                .filter(t -> !ignored.contains(t.getClass()))
                .toList();

        var categoryToCommand = myCommands.stream().collect(Collectors.groupingBy(MyCommand::getCategory));
        List<CommandData> categoryCommands = categoryToCommand.entrySet().stream().filter(t -> categorized.contains(t.getKey())).map((k) -> k.getValue().stream().reduce(new CommandData(k.getKey().getPrefix(), k.getKey().getDescription()),
                (commandData, myCommand) -> {
                    SubcommandData subcommandData = processSubComand(myCommand);
                    commandData.addSubcommand(subcommandData);
                    return commandData;
                },
                (c, d) -> {
                    d.getSubcommands().forEach(c::addSubcommand);
                    return c;
                })).toList();

        myCommands = myCommands.stream().filter(not(t -> categorized.contains(t.getCategory()))).toList();

        // Fake time category
        CommandData timeCommands = myCommands.stream().filter(t -> timeGrouped.contains(t.getClass())).reduce(new CommandData("time", "Commands that use timed data"),
                (commandData, myCommand) -> {
                    SubcommandData subcommandData = processSubComand(myCommand);
                    commandData.addSubcommand(subcommandData);
                    return commandData;
                },
                (c, d) -> {
                    d.getSubcommands().forEach(c::addSubcommand);
                    return c;
                });


        //noinspection ResultOfMethodCallIgnored
        myCommands.stream().filter(t -> !timeGrouped.contains(t.getClass()))
                .map(InteractionBuilder::processCommand)
                .forEach(commandUpdateAction::addCommands);

        // Generate one for fm
        //noinspection ResultOfMethodCallIgnored
        myCommands.stream().filter(t -> t instanceof NowPlayingCommand)
                .map(InteractionBuilder::processCommand).findFirst()
                .map(t -> t.setName("fm")).ifPresent(commandUpdateAction::addCommands);


        categoryCommands.stream().collect(Collectors.toMap(t -> t, InteractionBuilder::countCommand)).forEach((t, k) -> {
            if (k > 4000) {
                throw new IllegalStateException("Slash command has more than 4k characters: %s".formatted(t.getName()));
            }
        });

        return commandUpdateAction
                .addCommands(categoryCommands)
                .addCommands(timeCommands);
    }

    private static int countCommand(CommandData commandData) {
        return commandData.getName().length() +
               commandData.getDescription().length() +
               countOptions(commandData.getOptions()) +
               commandData.getSubcommands().stream().mapToInt(InteractionBuilder::countSubcommand).sum();

    }

    private static int countSubcommand(SubcommandData subcommandData) {
        return subcommandData.getName().length() + subcommandData.getDescription().length() + countOptions(subcommandData.getOptions());
    }

    private static int countOptions(List<OptionData> optionData) {
        return optionData.stream().mapToInt(t -> t.getDescription().length() + t.getName().length() + t.getChoices().stream().map(Command.Choice::getName).mapToInt(String::length).sum()).sum();
    }

    @NotNull
    private static SubcommandData processSubComand(MyCommand<?> myCommand) {
        SubcommandData commandData = new SubcommandData(myCommand.getAliases().get(0), StringUtils.abbreviate(myCommand.getDescription(), 100));
        List<Explanation> usages = myCommand.getParser().getUsages();
        usages.forEach(t -> t.explanation().options().forEach(commandData::addOption));
        processOpts(myCommand, commandData::addOption);
        return commandData;
    }

    @NotNull
    private static CommandData processCommand(MyCommand<?> myCommand) {
        CommandData commandData = new CommandData(myCommand.getAliases().get(0), StringUtils.abbreviate(myCommand.getDescription(), 100));
        List<Explanation> usages = myCommand.getParser().getUsages();
        usages.forEach(t -> t.explanation().options().forEach(commandData::addOption));
        processOpts(myCommand, commandData::addOption);
        return commandData;
    }

    private static void processOpts(MyCommand<?> myCommand, Consumer<OptionData> consumer) {
        List<OptionalEntity> optionals = myCommand.getParser().getOptionals();
        optionals.stream().filter(t -> !t.isEnabledByDefault()).forEach(t -> {
            OptionData data = new OptionData(OptionType.STRING, t.getValue(), t.getDescription());
            data.addChoice("yes", "yes");
            consumer.accept(data);
        });
    }


}
