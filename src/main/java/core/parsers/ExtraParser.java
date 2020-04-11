package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import core.parsers.params.ExtraParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * @param <Z> The Subclass of CommandParameters to be returned after everything
 * @param <Y> The Subclass of CommandParameters that the inner parser returns
 * @param <T> THe Inner Parser class
 * @param <J> The class of the item in the extra parser
 */
public class ExtraParser<Z extends ExtraParameters<Y, J>, Y extends CommandParameters, T extends Parser<Y>, J> extends Parser<Z> {

    private final T innerParser;
    private final J def;
    private final Predicate<String> predicate;
    private final Predicate<J> checkPredicate;
    public static final int LIMIT_ERROR = 999;
    public static final int INNER_ERROR = 1000;
    private final Function<List<J>, J> chooserPredicate;

    private final Function<String, J> fromString;
    private final String fieldName;
    private final String fieldDescription;
    private Function<J, String> toString;
    private final boolean panicOnMultiple;
    private final boolean catchFirst;

    private final BiPredicate<Y, J> innerPredicate;
    private final BiFunction<Y, J, Z> finalReducer;

    public ExtraParser(T innerParser,
                       J defaultItem,
                       Predicate<String> matchingItems,
                       Predicate<J> safetyPredicate,
                       Function<String, J> fromString,
                       Function<J, String> toString,
                       Map<Integer, String> errorMessages,
                       String fieldName,
                       String fieldDescription,
                       BiFunction<Y, J, Z> finalReducer) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, toString, errorMessages, fieldName, fieldDescription, null, finalReducer);
    }


    public ExtraParser(T innerParser,
                       J defaultItem,
                       Predicate<String> matchingItems,
                       Predicate<J> safetyPredicate,
                       Function<String, J> fromString,
                       Function<J, String> toString,
                       Map<Integer, String> errorMessages,
                       String fieldName,
                       String fieldDescription,
                       BiPredicate<Y, J> innerPredicate, BiFunction<Y, J, Z> finalReducer) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, toString, errorMessages, fieldName, fieldDescription, innerPredicate, null, true, false, finalReducer);
    }


    public ExtraParser(T innerParser,
                       J defaultItem,
                       Predicate<String> matchingItems,
                       Predicate<J> safetyPredicate,
                       Function<String, J> fromString,
                       Function<J, String> toString,
                       Map<Integer, String> errorMessages,
                       String fieldName,
                       String fieldDescription,
                       BiPredicate<Y, J> innerPredicate,
                       Function<List<J>, J> chooserPredicate,
                       boolean panicOnMultiple, boolean catchFirst, BiFunction<Y, J, Z> finalReducer) {
        super();
        this.innerParser = innerParser;
        def = defaultItem;
        this.predicate = matchingItems;
        this.checkPredicate = safetyPredicate;
        this.fromString = fromString;
        this.toString = toString;
        this.fieldName = fieldName;
        this.fieldDescription = fieldDescription;
        this.innerPredicate = innerPredicate;
        this.catchFirst = catchFirst;
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
    protected Z parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        Map<Boolean, List<String>> collect = Arrays.stream(words).collect(Collectors.partitioningBy((predicate)));
        List<J> first = collect.get(true).stream().map(fromString).collect(Collectors.toList());
        List<String> returning = collect.get(false);
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
                collect.get(true).stream().filter(x -> !fromString.apply(x).equals(item)).forEach(returning::add);
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
    public String getUsageLogic(String commandName) {
        String usageLogic = innerParser.getUsageLogic(commandName);
        int i = usageLogic.indexOf('\n');
        String substring1 = usageLogic.substring(0, i - 2);
        String substring2 = usageLogic.substring(i);

        return substring1 + " *" + fieldName + "***" + substring2 + "\t" + fieldDescription + "\n";
    }
}
