package core.parsers;

import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @param <K> The Subclass of CommandParameters that will be embededed on ExtraParameters
 * @param <T> The parser that returns the type K
 **/
public class NumberParser<K extends CommandParameters, T extends Parser<K>> extends ExtraParser<NumberParameters<K>, K, T, Long> {
    public static final Pattern compile = Pattern.compile("[1-9]([0-9]+)?");
    public static final Pattern allow0 = Pattern.compile("[0-9]+");

    public static final Predicate<String> predicate = compile.asMatchPredicate();

    //TODO Builder
    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String fieldDescription,
                        String fieldName,
                        BiPredicate<K, Long> innerPredicate
    ) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                fieldName,
                fieldDescription, innerPredicate,
                (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));

    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                "number",
                description,
                (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        boolean catchFirst
    ) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                "number",
                description,
                null,
                null,
                panicOnFailure,
                catchFirst, (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        Function<List<Long>, Long> accum) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                "number",
                description,
                null,
                accum,
                panicOnFailure,
                false, (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        boolean catchFirst,
                        boolean allow0
    ) {
        super(innerParser,
                defaultItem,
                allow0 ? NumberParser.allow0.asMatchPredicate() : predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                "number",
                description,
                null,
                null,
                panicOnFailure,
                catchFirst, (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));
    }

}
