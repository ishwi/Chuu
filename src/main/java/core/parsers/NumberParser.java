package core.parsers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NumberParser<T extends Parser> extends ExtraParser<T, Long> {
    public static Pattern compile = Pattern.compile("[1-9][0-9]+");
    public static Predicate<String> predicate = (s) -> compile.matcher(s).matches();


    public NumberParser(T innerParser, Long defaultItem, long max, Map<Integer, String> errorMessages, String description) {
        super(innerParser, defaultItem, predicate, (number) -> number > max || number < 0, Long::parseLong, String::valueOf, errorMessages, "number", description);
    }

    public NumberParser(T innerParser, Long defaultItem, long max, Map<Integer, String> errorMessages, String description, boolean panicOnFailure, Function<List<Long>, Long> accum) {
        super(innerParser, defaultItem, predicate, (number) -> number > max || number < 0, Long::parseLong, String::valueOf, errorMessages, "number", description, null, accum, panicOnFailure);
    }
}
