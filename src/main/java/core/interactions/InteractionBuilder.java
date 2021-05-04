package core.interactions;

import core.commands.abstracts.MyCommand;
import core.commands.config.HelpCommand;
import core.commands.discovery.FeaturedCommand;
import core.commands.discovery.RandomAlbumCommand;
import core.commands.moderation.InviteCommand;
import core.commands.scrobble.LoginCommand;
import core.commands.stats.HardwareStatsCommand;
import core.commands.stats.SourceCommand;
import core.commands.stats.TopCombosCommand;
import core.parsers.*;
import core.parsers.explanation.util.Explanation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.CheckReturnValue;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class InteractionBuilder {
    private static final Set<Class<? extends Parser<?>>> parsers = Set.of(ArtistParser.class, NpParser.class, ArtistAlbumParser.class, ArtistSongParser.class, ChartParser.class, OnlyUsernameParser.class, SetParser.class, TwoUsersTimeframeParser.class, TwoUsersParser.class);
    private static final Set<Class<? extends MyCommand<?>>> commands = Set.of(HelpCommand.class, SourceCommand.class, InviteCommand.class, LoginCommand.class, TopCombosCommand.class, HardwareStatsCommand.class, FeaturedCommand.class, RandomAlbumCommand.class);

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

    private static CommandUpdateAction fillAction(JDA jda, CommandUpdateAction commandUpdateAction) {
        List<? extends MyCommand<?>> myCommands = jda.getRegisteredListeners().stream()
                .filter(t -> t instanceof MyCommand<?>)
                .map(t -> (MyCommand<?>) t)
                .sorted(Comparator.comparingInt(t -> t.order))
                .filter(t -> parsers.contains(t.getParser().getClass()) || commands.contains(t.getClass()))
                .limit(100)
                .toList();
        for (MyCommand<?> myCommand : myCommands) {
            try {
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
                    data.addChoice("yes", "true");
                    commandData.addOption(data);
                });

                //noinspection ResultOfMethodCallIgnored
                commandUpdateAction.addCommands(commandData);
            } catch (Exception a) {
                a.printStackTrace();
                System.out.println(a.getMessage());
                System.out.println(myCommand.getName());
            }
        }
        return commandUpdateAction;
    }

}
