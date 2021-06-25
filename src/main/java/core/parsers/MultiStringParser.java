package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.commands.abstracts.MyCommand;
import core.exceptions.LastFmException;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.MultiExplanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.parsers.ParserAux.digitMatcher;

public abstract class MultiStringParser<T extends CommandParameters> extends DaoParser<T> implements Generable<T> {
    private final String entityName;
    private final String entityNamePlural;

    public MultiStringParser(ChuuService dao, String entityName, String entityNamePlural, OptionalEntity... opts) {
        super(dao, opts);
        this.entityName = entityName;
        this.entityNamePlural = entityNamePlural;
    }


    @Override
    public T parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        Set<String> strs = e.getOptionsByType(OptionType.STRING).stream().map(OptionMapping::getAsString).collect(Collectors.toSet());
        if (strs.isEmpty()) {
            int count = Optional.ofNullable(e.getOption("from-np")).map(OptionMapping::getAsLong).map(value -> {
                if (value > 15) {
                    return null;
                }
                return Math.toIntExact(value);
            }).orElse(2);
            return doSomethingNoWords(count, findLastfmFromID(user, ctx), ctx);
        }
        return doSomethingWords(findLastfmFromID(user, ctx), ctx, strs);
    }

    @Override
    public List<Explanation> getUsages() {
        return MultiExplanation.obtainMultiExplanation(entityName, entityNamePlural, List.of(new StrictUserExplanation()));
    }

    @Override
    protected T parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        LastFMData lastFMData = findLastfmFromID(oneUser, e);
        Pair<String[], Integer> integerPair = filterMessage(words, digitMatcher.asMatchPredicate(), Integer::parseInt, 2);
        int x = integerPair.second;
        if (x > 15) {
            sendError("Can't do more than 15 " + entityNamePlural, e);
            return null;
        }
        words = integerPair.first;
        if (words.length == 0) {
            return doSomethingNoWords(x, lastFMData, e);

        } else {
            String str = String.join(" ", getSubMessage(e));
            String[] split = str.split("(?<!\\\\)\\s*[|-]\\s*");
            Set<String> set = Stream.of(split).map(t ->
                    t.trim().replaceAll("\\\\(|-)", "$1")
            ).collect(Collectors.toSet());
            if (set.size() > 15) {
                sendError("Can't do more than 15 " + entityNamePlural, e);
                return null;
            }
            return doSomethingWords(lastFMData, e, set);

        }
    }

    protected abstract T doSomethingNoWords(int limit, LastFMData lastFMData, Context e) throws InstanceNotFoundException, LastFmException;

    protected abstract T doSomethingWords(LastFMData lastFMData, Context e, Set<String> strings);

    @Override
    public CommandData generateCommandData(MyCommand<?> myCommand) {
        CommandData commandData = new CommandData(myCommand.slashName(), myCommand.getDescription());
        commandData.addOption(OptionType.INTEGER, "from-np", "# of " + entityNamePlural + " to fetch from np");

        for (int i = 1; i <= 5; i++) {
            commandData.addOption(OptionType.STRING, entityName + "-" + i, EmbedBuilder.ZERO_WIDTH_SPACE);
        }
        return commandData;
    }
}
