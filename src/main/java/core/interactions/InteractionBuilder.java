package core.interactions;

import core.Chuu;
import core.commands.abstracts.MyCommand;
import core.commands.artists.TimeOnArtistCommand;
import core.commands.charts.*;
import core.commands.config.GuildConfigCommand;
import core.commands.config.UserConfigCommand;
import core.commands.moderation.EvalCommand;
import core.commands.moderation.MbidUpdatedCommand;
import core.commands.moderation.RefreshSlashCommand;
import core.commands.random.RandomAlbumCommand;
import core.commands.stats.*;
import core.commands.utils.CommandCategory;
import core.parsers.Generable;
import core.parsers.explanation.UrlExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.utils.OptionalEntity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class InteractionBuilder {
    public static final Predicate<MyCommand<?>> test = t -> {
        try {
            t.getParser().parseSlashLogic(null);
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        } catch (Exception e) {
            return true;
        }
    };
    private static final Set<CommandCategory> categorized = EnumSet.of(
            CommandCategory.RYM,
            CommandCategory.BOT_INFO,
            CommandCategory.CROWNS,
            CommandCategory.UNIQUES,
            CommandCategory.CHARTS,
            CommandCategory.SERVER_LEADERBOARDS,
            CommandCategory.BOT_STATS,
            CommandCategory.SERVER_STATS,
            CommandCategory.TRENDS,
            CommandCategory.STREAKS,
            CommandCategory.GENRES,
            CommandCategory.CONFIGURATION,
            CommandCategory.MODERATION,
            CommandCategory.STARTING,
            CommandCategory.MUSIC,
            CommandCategory.RANDOM,
            CommandCategory.DISCOVERY
    );
    private static final Set<Class<? extends MyCommand<?>>> ignored = Set.of(EvalCommand.class, MbidUpdatedCommand.class, RefreshSlashCommand.class,
            UserConfigCommand.class, GuildConfigCommand.class);
    private static final Set<Class<? extends MyCommand<?>>> timeGrouped =
            Set.of(WastedChartCommand.class,
                    WastedTrackCommand.class,
                    FirstArtistCommand.class,
                    FirstPlayedCommand.class,
                    TimeOnArtistCommand.class,
                    LastPlayedArtistCommand.class,
                    LastPlayedCommand.class,
                    TimeSpentCommand.class,
                    WastedAlbumChartCommand.class,
                    WeeklyCommand.class,
                    DailyCommand.class);
    private static final Set<Class<? extends MyCommand<?>>> colorGrouped =
            Set.of(GayCommand.class,
                    RainbowChartCommand.class,
                    ColorChartCommand.class);

    @CheckReturnValue
    public static CommandListUpdateAction setGlobalCommands(JDA jda) {
        CommandListUpdateAction commandUpdateAction = jda.updateCommands();
        CommandListUpdateAction firstBatch = fillAction(jda, commandUpdateAction);
        return UserCommandsBuilder.fillAction(jda, firstBatch);
    }

    @CheckReturnValue
    public static CommandListUpdateAction setServerCommand(Guild guild) {
        CommandListUpdateAction commandUpdateAction = guild.updateCommands();

        return fillAction(guild.getJDA(), commandUpdateAction);
    }

    private static CommandListUpdateAction fillAction(JDA jda, CommandListUpdateAction commandUpdateAction) {
        Map<String, MyCommand<?>> commandMap = new HashMap<>();
        List<? extends MyCommand<?>> myCommands = jda.getRegisteredListeners().stream()
                .filter(t -> t instanceof MyCommand<?>)
                .map(t -> (MyCommand<?>) t)
                .sorted(Comparator.comparingInt(t -> t.order))
                .filter(test)
                .filter(t -> !ignored.contains(t.getClass()))
                .toList();

        List<SlashCommandData> tmp = new ArrayList<>();


        SlashCommandData timeCommands = myCommands.stream().filter(t -> timeGrouped.contains(t.getClass())).reduce(
                Commands.slash("time", "Commands that use timed data"),
                (commandData, myCommand) -> {
                    SubcommandData subcommandData = processSubComand(myCommand);
                    commandMap.put(commandData.getName() + '/' + subcommandData.getName(), myCommand);
                    commandData.addSubcommands(subcommandData);
                    return commandData;
                },
                (c, d) -> {
                    c.addSubcommands(d.getSubcommands());
                    return c;
                });
        myCommands = myCommands.stream().filter(t -> !timeGrouped.contains(t.getClass())).toList();
        SlashCommandData colorCommands = myCommands.stream().filter(t -> colorGrouped.contains(t.getClass())).reduce(
                Commands.slash("colour", "Charts that use colour of images"),
                (commandData, myCommand) -> {
                    SubcommandData subcommandData = processSubComand(myCommand);
                    commandMap.put(commandData.getName() + '/' + subcommandData.getName(), myCommand);
                    commandData.addSubcommands(subcommandData);
                    return commandData;
                },
                (c, d) -> {
                    c.addSubcommands(d.getSubcommands());
                    return c;
                });
        myCommands = myCommands.stream().filter(t -> !colorGrouped.contains(t.getClass())).toList();

        Map<SlashCommandData, MyCommand<?>> toBeprocessed = new HashMap<>();
        var categoryToCommand = myCommands.stream().collect(Collectors.groupingBy(MyCommand::getCategory,
                () -> new EnumMap<>(CommandCategory.class),
                Collectors.toList()
        ));
        List<SlashCommandData> categoryCommands = categoryToCommand.entrySet().stream().filter(t -> categorized.contains(t.getKey())).map((k) -> k.getValue().stream().reduce(
                Commands.slash(k.getKey().getPrefix(), k.getKey().getDescription()),
                (commandData, myCommand) -> {
                    SubcommandData subcommandData;
                    if (myCommand instanceof RandomAlbumCommand ra) {

                        Map<Boolean, List<Explanation>> collect = ra.getParser().getUsages().stream().collect(Collectors.partitioningBy(z -> Objects.equals(z.explanation().header(), UrlExplanation.NAME)));
                        List<Explanation> explanationsSubmit = collect.get(true);
                        List<Explanation> explanationsGet = collect.get(false);

                        SubcommandData submit = generateSubData(myCommand);
                        insertUsage(submit, explanationsSubmit);
                        submit.setName("submit");

                        SubcommandData get = generateSubData(myCommand);
                        get.setName("obtain");
                        insertUsage(get, explanationsGet);
                        processOpts(myCommand, get::addOptions);

                        commandMap.put(commandData.getName() + "/obtain", ra);
                        commandMap.put(commandData.getName() + "/submit", ra);
                        commandData.addSubcommands(get, submit);
                        return commandData;
                    } else if (myCommand.getParser() instanceof Generable<?> w) {
                        SlashCommandData gen = w.generateCommandData(myCommand);
                        if (!gen.getSubcommands().isEmpty()) {
                            toBeprocessed.put(gen, myCommand);
                            return commandData;
                        }
                        subcommandData = new SubcommandData(gen.getName(), gen.getDescription()).addOptions(gen.getOptions());
                    } else {
                        subcommandData = processSubComand(myCommand);
                    }
                    commandMap.put(commandData.getName() + '/' + subcommandData.getName(), myCommand);
                    commandData.addSubcommands(subcommandData);
                    return commandData;
                },
                (c, d) -> {
                    c.addSubcommands(d.getSubcommands());
                    return c;
                })).toList();
        toBeprocessed.forEach((a, j) -> {
            commandMap.put(a.getName(), j);
            tmp.add(a);
        });
        myCommands = myCommands.stream().filter(not(t -> categorized.contains(t.getCategory()))).toList();

        List<SlashCommandData> generables = myCommands.stream().filter(t -> (t.getParser() instanceof Generable<?>)).map(z -> {
            Generable<?> generable = (Generable<?>) z.getParser();
            SlashCommandData commandData = generable.generateCommandData(z);
            commandData.getSubcommands().forEach(w -> commandMap.put(commandData.getName() + '/' + w.getName(), z));
            return commandData;
        }).toList();

        myCommands = myCommands.stream().filter(t -> !(t.getParser() instanceof Generable<?>)).toList();


        // Fake time category

        myCommands.stream()
                .map(InteractionBuilder::processCommand)
                .forEach(tmp::add);


        // Generate one for fm
        myCommands.stream().filter(t -> t instanceof NowPlayingCommand)
                .map(InteractionBuilder::processCommand).findFirst()
                .map(t -> t.setName("fm")).ifPresent(tmp::add);


        jda.getRegisteredListeners().stream()
                .filter(t -> t instanceof MyCommand<?>)
                .map(t -> (MyCommand<?>) t).filter(t -> t instanceof UserConfigCommand || t instanceof GuildConfigCommand)
                .map(z -> {
                    Generable<?> parser = (Generable<?>) z.getParser();
                    SlashCommandData commandData = parser.generateCommandData(z);
                    commandData.getSubcommands().forEach(r -> commandMap.put(commandData.getName() + "/" + r.getName(), z));
                    return commandData;
                }).forEach(tmp::add);


        tmp.addAll(categoryCommands);
        tmp.add(timeCommands);
        tmp.add(colorCommands);
        tmp.addAll(generables);

        tmp.stream().collect(Collectors.toMap(t -> t, InteractionBuilder::countCommand)).forEach((t, k) -> {
            if (k > 4000) {
                throw new IllegalStateException("Slash command has more than 4k characters: %s".formatted(t.getName()));
            }
        });

        Map<String, Long> collect = tmp.stream().mapMulti((SlashCommandData a, Consumer<String> b) -> {
            if (a.getSubcommands().isEmpty()) {
                b.accept(a.getName());
            } else {
                a.getSubcommands().forEach(w -> b.accept(a.getName() + "/" + w.getName()));
            }
        }).collect(Collectors.groupingBy(z -> z, Collectors.counting()));


        List<Map.Entry<String, Long>> entries = collect.entrySet().stream().filter(z -> z.getValue() > 1).toList();
        for (Map.Entry<String, Long> entry : entries) {
            System.out.println(entry.getKey());
        }
        if (!entries.isEmpty()) {
            throw new IllegalStateException("Slash commands repeated!");
        }

        Chuu.customManager.addSlashVariants(commandMap);

        return commandUpdateAction.addCommands(tmp);
    }

    private static int countCommand(SlashCommandData commandData) {
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

    private static int countCommandData(SlashCommandData commandData) {
        return commandData.toData().toString().length();
    }

    @Nonnull
    static SubcommandData processSubComand(MyCommand<?> myCommand) {
        SubcommandData commandData = generateSubData(myCommand);
        List<Explanation> usages = myCommand.getParser().getUsages();
        insertUsage(commandData, usages);
        processOpts(myCommand, commandData::addOptions);
        return commandData;
    }

    @NotNull
    private static SubcommandData generateSubData(MyCommand<?> myCommand) {
        return new SubcommandData(myCommand.slashName(), StringUtils.abbreviate(myCommand.getDescription(), 100));
    }

    private static void insertUsage(SubcommandData commandData, List<Explanation> usages) {
        commandData.addOptions(usages.stream().flatMap(t -> t.explanation().options()
                .stream()
                .map(z -> z.setDescription(z.getDescription().replace("If the size is not specified it defaults to 5x5", "Size of the chart")))
        ).toList());
    }

    @Nonnull
    private static SlashCommandData processCommand(MyCommand<?> myCommand) {
        SlashCommandData commandData = Commands.slash(myCommand.slashName(), StringUtils.abbreviate(myCommand.getDescription(), 100));
        List<Explanation> usages = myCommand.getParser().getUsages();
        usages.forEach(t -> commandData.addOptions(new ArrayList<>(t.explanation().options())));
        processOpts(myCommand, commandData::addOptions);
        return commandData;
    }

    public static void processOpts(MyCommand<?> myCommand, Consumer<OptionData> consumer) {
        processOpts(myCommand.getParser().getOptionals(), consumer);

    }

    public static void processOpts(List<OptionalEntity> opts, Consumer<OptionData> consumer) {
        opts.stream().filter(t -> !t.isEnabledByDefault()).forEach(t -> {
            OptionData data = new OptionData(OptionType.STRING,
                    t.value()

                    , t.definition().replace("Can be use to", "").strip());
            data.addChoice("yes", "yes");
            consumer.accept(data);
        });
    }


}
