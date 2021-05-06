package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import core.parsers.params.ExtraParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @param <Z> The Subclass of CommandParameters to be returned after everything
 * @param <Y> The Subclass of CommandParameters that the inner parser returns
 * @param <T> THe Inner Parser class
 * @param <J> The class of the item in the extra parser
 */
public class ExtraParser<Z extends ExtraParameters<Y, @NotNull J>, Y extends CommandParameters, T extends Parser<Y>, J> extends Parser<Z> {

    public static final int LIMIT_ERROR = 999;
    public static final int INNER_ERROR = 1000;
    private final T innerParser;
    private final J def;
    private final Predicate<String> predicate;
    private final Predicate<J> checkPredicate;
    private final Function<List<J>, J> chooserPredicate;

    private final Function<String, J> fromString;
    private final List<Explanation> explanations;
    private final boolean panicOnMultiple;
    private final boolean catchFirst;
    private final BiPredicate<Y, J> innerPredicate;
    private final Function<SlashCommandEvent, J> fromSlash;
    private final BiFunction<Y, J, Z> finalReducer;

    public ExtraParser(T innerParser,
                       J defaultItem,
                       Predicate<String> matchingItems,
                       Predicate<J> safetyPredicate,
                       Function<String, J> fromString,
                       Map<Integer, String> errorMessages,
                       BiFunction<Y, J, Z> finalReducer, List<Explanation> explanations, Function<SlashCommandEvent, J> fromSlash) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, errorMessages, null, finalReducer, explanations, fromSlash);
    }


    public ExtraParser(T innerParser,
                       J defaultItem,
                       Predicate<String> matchingItems,
                       Predicate<J> safetyPredicate,
                       Function<String, J> fromString,
                       Map<Integer, String> errorMessages,
                       BiPredicate<Y, J> innerPredicate, BiFunction<Y, J, Z> finalReducer, List<Explanation> explanations, Function<SlashCommandEvent, J> fromSlash) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, errorMessages, explanations, innerPredicate, null, true, false, fromSlash, finalReducer);
    }


    public ExtraParser(T innerParser,
                       J defaultItem,
                       Predicate<String> matchingItems,
                       Predicate<J> safetyPredicate,
                       Function<String, J> fromString,
                       Map<Integer, String> errorMessages,
                       List<Explanation> explanations, BiPredicate<Y, J> innerPredicate,
                       Function<List<J>, J> chooserPredicate,
                       boolean panicOnMultiple, boolean catchFirst, Function<SlashCommandEvent, J> fromSlash, BiFunction<Y, J, Z> finalReducer) {
        super();
        this.innerParser = innerParser;
        def = defaultItem;
        this.predicate = matchingItems;
        this.checkPredicate = safetyPredicate;
        this.fromString = fromString;
        this.explanations = explanations;
        this.innerPredicate = innerPredicate;
        this.catchFirst = catchFirst;
        this.fromSlash = fromSlash;
        this.finalReducer = finalReducer;
        this.opts.addAll(innerParser.opts);
        this.errorMessages.putAll(innerParser.errorMessages);
        this.errorMessages.putAll(errorMessages);
        this.chooserPredicate = chooserPredicate;
        this.panicOnMultiple = panicOnMultiple;
    }

    @Override
    protected void setUpErrorMessages() {
        //Ovverriding
    }

    @Override
    public Z parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {

        J item = fromSlash.apply(ctx.e());
        if (item == null) {
            item = def;
        } else if (checkPredicate.test(item)) {
            this.sendError(this.getErrorMessage(LIMIT_ERROR), ctx);
            return null;
        }
        Y y = innerParser.parse(ctx);
        if (y == null) {
            return null;
        }
        if (this.innerPredicate != null && item != null) {
            if (innerPredicate.test(y, item)) {
                this.sendError(this.getErrorMessage(INNER_ERROR), ctx);
                return null;
            }
        }
        return finalReducer.apply(y, item);

    }

    @Override
    protected Z parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        Map<Boolean, List<String>> predicateToLines = Arrays.stream(words).collect(Collectors.partitioningBy((predicate)));
        List<J> first = predicateToLines.get(true).stream().map(fromString).toList();
        List<String> returning = predicateToLines.get(false);
        J item;
        if (first.isEmpty()) {
            item = def;
        } else if (first.size() > 1) {
            if (panicOnMultiple) {
                this.sendError("You introduced too many things", e);
                return null;
            }
            if (catchFirst) {
                item = first.get(0);
            } else {
                item = chooserPredicate.apply(first);
                predicateToLines.get(true).stream().filter(x -> !fromString.apply(x).equals(item)).forEach(returning::add);
            }
        } else {
            item = first.get(0);
            if (checkPredicate.test(item)) {
                this.sendError(this.getErrorMessage(LIMIT_ERROR), e);
                return null;
            }
        }
        Y y = innerParser.parseLogic(e, returning.toArray(String[]::new));
        if (y == null)
            return null;
        if (this.innerPredicate != null && item != null) {
            if (innerPredicate.test(y, item)) {
                this.sendError(this.getErrorMessage(INNER_ERROR), e);
                return null;
            }
        }
        return finalReducer.apply(y, item);
    }

    @Override
    public List<Explanation> getUsages() {
        return Stream.of(innerParser.getUsages(), this.explanations).flatMap(Collection::stream).toList();
    }


}
