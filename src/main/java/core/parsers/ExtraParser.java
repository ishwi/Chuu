package core.parsers;

import com.google.common.collect.ObjectArrays;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExtraParser<T extends Parser, J> extends Parser {

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
    private BiPredicate<String[], J> innerPredicate;

    public ExtraParser(T innerParser, J defaultItem, Predicate<String> matchingItems, Predicate<J> safetyPredicate, Function<String, J> fromString, Function<J, String> toString, Map<Integer, String> errorMessages, String fieldName, String fieldDescription) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, toString, errorMessages, fieldName, fieldDescription, null);
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
                       BiPredicate<String[], J> innerPredicate) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, toString, errorMessages, fieldName, fieldDescription, innerPredicate, null, true);
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
                       BiPredicate<String[], J> innerPredicate,
                       boolean panicOnMultiple) {
        this(innerParser, defaultItem, matchingItems, safetyPredicate, fromString, toString, errorMessages, fieldName, fieldDescription, innerPredicate, (x) -> x.get(0), panicOnMultiple);
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
                       BiPredicate<String[], J> innerPredicate,
                       Function<List<J>, J> chooserPredicate,
                       boolean panicOnMultiple) {
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
        this.opts.addAll(innerParser.opts);
        this.errorMessages.putAll(innerParser.errorMessages);
        this.errorMessages.putAll(errorMessages);
        this.chooserPredicate = chooserPredicate;
        this.panicOnMultiple = panicOnMultiple;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        Map<Boolean, List<String>> collect = Arrays.stream(words).collect(Collectors.partitioningBy((predicate)));
        List<J> first = collect.get(true).stream().map(fromString).collect(Collectors.toList());
        List<String> returning = collect.get(false);
        J item;
        if (first.size() == 0) {
            item = def;
        } else if (first.size() > 1) {
            if (panicOnMultiple) {
                this.sendError("You introduced too many things", e);
                return null;
            }
            item = chooserPredicate.apply(first);
            collect.get(true).stream().filter(x -> !fromString.apply(x).equals(item)).forEach(returning::add);
        } else {
            item = first.get(0);
            if (checkPredicate.test(item)) {
                this.sendError(this.getErrorMessage(LIMIT_ERROR), e);
                return null;
            }
        }
        String[] strings1 = innerParser.parseLogic(e, returning.toArray(String[]::new));
        if (this.innerPredicate != null && item != null) {
            if (innerPredicate.test(strings1, item)) {
                this.sendError(this.getErrorMessage(INNER_ERROR), e);
                return null;
            }
        }
        return ObjectArrays.concat(toString.apply(item), strings1);

    }


    @Override
    public String getUsageLogic(String commandName) {
        String usageLogic = innerParser.getUsageLogic(commandName);
        int i = usageLogic.indexOf("\n");
        String substring1 = usageLogic.substring(0, i - 1);
        String substring2 = usageLogic.substring(i);

        return substring1 + " *" + fieldName + "*" + substring2 + "\t" + fieldDescription + "\n";
    }
}
