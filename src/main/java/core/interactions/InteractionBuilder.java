package core.interactions;

import core.commands.abstracts.MyCommand;
import core.commands.utils.CommandCategory;
import core.parsers.OptionalEntity;
import core.parsers.explanation.util.Explanation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
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
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class InteractionBuilder {
    private static final Set<CommandCategory> grouped = Set.of(CommandCategory.RYM, CommandCategory.BOT_INFO, CommandCategory.CONFIGURATION, CommandCategory.MODERATION, CommandCategory.STARTING, CommandCategory.MUSIC);

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
                .toList();

        var b = myCommands.stream().collect(Collectors.groupingBy(MyCommand::getCategory));
        List<CommandData> commandData1 = b.entrySet().stream().filter(t -> grouped.contains(t.getKey())).map((k) -> k.getValue().stream().reduce(new CommandData(k.getKey().toString().replaceAll("_", "-").toLowerCase(Locale.ROOT), k.getKey().getDescription()),
                (commandData, myCommand) -> {
                    SubcommandData subcommandData = processSubComand(myCommand);
                    commandData.addSubcommand(subcommandData);
                    return commandData;
                },
                (c, d) -> {
                    d.getSubcommands().forEach(c::addSubcommand);
                    return c;
                })).toList();

        myCommands = myCommands.stream().filter(not(t -> grouped.contains(t.getCategory()))).toList();
        for (MyCommand<?> myCommand : myCommands) {
            CommandData commandData = processCommand(myCommand);

            //noinspection ResultOfMethodCallIgnored
            commandUpdateAction.addCommands(commandData);
        }
        //noinspection ResultOfMethodCallIgnored
        commandUpdateAction.addCommands(commandData1);
        return commandUpdateAction;
    }

    @NotNull
    private static SubcommandData processSubComand(MyCommand<?> myCommand) {
        SubcommandData commandData = new SubcommandData(myCommand.getAliases().get(0), StringUtils.abbreviate(myCommand.getDescription(), 100));
        List<Explanation> usages = myCommand.getParser().getUsages();
        usages.forEach(t -> {
            OptionData optionData = t.explanation().optionData();
            if (optionData != null) {
                commandData.addOption(optionData);
            } else {
                System.out.println(t.explanation().header());
            }
        });
        List<OptionalEntity> optionals = myCommand.getParser().getOptionals();
        optionals.stream().filter(t -> !t.isEnabledByDefault()).forEach(t -> {
            OptionData data = new OptionData(OptionType.STRING, t.getValue(), t.getDescription());
            data.addChoice("yes", "true");
            commandData.addOption(data);
        });
        return commandData;
    }

    @NotNull
    private static CommandData processCommand(MyCommand<?> myCommand) {
        CommandData commandData = new CommandData(myCommand.getAliases().get(0), StringUtils.abbreviate(myCommand.getDescription(), 100));
        List<Explanation> usages = myCommand.getParser().getUsages();
        usages.forEach(t -> {
            OptionData optionData = t.explanation().optionData();
            if (optionData != null) {
                commandData.addOption(optionData);
            } else {
                System.out.println(t.explanation().header());
            }
        });
        List<OptionalEntity> optionals = myCommand.getParser().getOptionals();
        optionals.stream().filter(t -> !t.isEnabledByDefault()).forEach(t -> {
            OptionData data = new OptionData(OptionType.STRING, t.getValue(), t.getDescription());
            data.addChoice("yes", "yes");
            commandData.addOption(data);
        });
        return commandData;
    }

}
