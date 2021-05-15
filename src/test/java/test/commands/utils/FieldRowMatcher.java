package test.commands.utils;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldRowMatcher {
    private final String title;
    private final Pattern pattern;
    private final Predicate<Matcher> predicate;

    public FieldRowMatcher(String title, Pattern pattern, Predicate<Matcher> predicate) {

        this.title = title;
        this.pattern = pattern;
        this.predicate = predicate;
    }

    public FieldRowMatcher(String title, Pattern pattern) {

        this.title = title;
        this.pattern = pattern;
        this.predicate = matcher -> true;
    }

    public static FieldRowMatcher numberField(String fieldName) {
        return new FieldRowMatcher(fieldName, Pattern.compile("(\\d+)"),
                matcher -> Long.parseLong(matcher.group(1)) >= 0);
    }

    public static FieldRowMatcher numberFieldFromRange(String fieldName, int range) {
        return new FieldRowMatcher(fieldName, Pattern.compile("(\\d+)"),
                matcher -> Long.parseLong(matcher.group(1)) >= range);
    }

    public static FieldRowMatcher matchesAnything(String fieldName) {
        return new FieldRowMatcher(fieldName, Pattern.compile("[\\s\\S]*"));
    }


    public String getTitle() {
        return title;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Predicate<Matcher> getPredicate() {
        return predicate;
    }
}
