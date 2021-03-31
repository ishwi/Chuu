package core.parsers;

import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;

import java.util.Collections;
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
    private static final Pattern digitMatcher = Pattern.compile("[1-9]([0-9]+)?[kKmM]?");
    private static final Pattern allow0 = Pattern.compile("[0-9]+[kKmM]?");
    private static final Predicate<String> predicate = digitMatcher.asMatchPredicate();

    //TODO Builder
    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        ExplanationLine explanationLine,
                        BiPredicate<K, Long> innerPredicate
    ) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                innerPredicate,
                (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong), Collections.singletonList(() -> explanationLine));

    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages, String description
    ) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong), Collections.singletonList(() -> new ExplanationLine("Number", description))
        );
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
                NumberParser::parseStr,
                errorMessages,
                Collections.singletonList(() -> new ExplanationLine("Number", description)),
                null,
                null,
                panicOnFailure,
                catchFirst, (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong)


        );
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
                NumberParser::parseStr,
                errorMessages,
                Collections.singletonList(() -> new ExplanationLine("Number", description)), null,
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
                NumberParser::parseStr,
                errorMessages,
                Collections.singletonList(() -> new ExplanationLine("Number", description)), null,
                null,
                panicOnFailure,
                catchFirst, (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));
    }

    public static Long parseStr(String str) {
        str = str.toLowerCase();
        int multiplier = 1;
        if (str.endsWith("k")) {
            str = str.substring(0, str.length() - 1);
            multiplier = 1_000;
        }
        if (str.endsWith("m")) {
            str = str.substring(0, str.length() - 1);
            multiplier = 1_000_000;
        }
        return Long.parseLong(str) * multiplier;
    }
}
