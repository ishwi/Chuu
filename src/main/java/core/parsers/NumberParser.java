package core.parsers;

import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
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
    private static final Function<Interactible, String> nameObtainer = s -> s.options().stream().filter(t -> t.getType() == OptionType.INTEGER).map(OptionData::getName).findFirst().orElse("number");
    private static final Function<String, Function<SlashCommandEvent, Long>> slash = s -> event ->
    {
        OptionMapping number = event.getOption(s);
        if (number == null) {
            return null;
        }
        return number.getAsLong();
    };

    //TODO Builder
    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        Interactible explanationLine,
                        BiPredicate<K, Long> innerPredicate, boolean reverseOrder
    ) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                Long::parseLong,
                errorMessages,
                innerPredicate,
                (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong), Collections.singletonList(() -> explanationLine), slash.apply(nameObtainer.apply(explanationLine)));
        this.reverseOrder = reverseOrder;
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
                        new NumberParameters<>(k.getE(), k, aLong), Collections.singletonList(() -> new ExplanationLineType("number", description, OptionType.INTEGER)),
                slash.apply("number"));
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        boolean catchFirst, boolean reverseOrder
    ) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                NumberParser::parseStr,
                errorMessages,
                Collections.singletonList(() -> new ExplanationLineType("number", description, OptionType.INTEGER)),
                null,
                null,
                panicOnFailure,
                catchFirst, slash.apply("number"), (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong)


        );
        this.setReverseOrder(reverseOrder);
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        boolean catchFirst
    ) {
        this(innerParser, defaultItem, max, errorMessages, description, panicOnFailure, catchFirst, false);
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        Function<List<Long>, Long> accum, String header) {
        super(innerParser,
                defaultItem,
                predicate,
                number -> number > max || number < 0,
                NumberParser::parseStr,
                errorMessages,
                Collections.singletonList(() -> new ExplanationLineType(header, description, OptionType.INTEGER)), null,
                accum,
                panicOnFailure,
                false, slash.apply(header), (k, aLong) ->
                        new NumberParameters<>(k.getE(), k, aLong));
    }

    public NumberParser(T innerParser,
                        Long defaultItem,
                        long max,
                        Map<Integer, String> errorMessages,
                        String description,
                        boolean panicOnFailure,
                        boolean catchFirst,
                        boolean allow0, String header
    ) {
        super(innerParser,
                defaultItem,
                allow0 ? NumberParser.allow0.asMatchPredicate() : predicate,
                number -> number > max || number < 0,
                NumberParser::parseStr,
                errorMessages,
                Collections.singletonList(() -> new ExplanationLineType(header, description, OptionType.INTEGER)), null,
                null,
                panicOnFailure,
                catchFirst, slash.apply(header), (k, aLong) ->
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

    @NotNull
    public static <T extends CommandParameters> Parser<NumberParameters<T>> generateThresholdParser(Parser<T> parser) {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "Use a threshold to require a set amount of plays for a crowns";
        return new NumberParser<>(parser,
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true, "threshold");
    }
}
