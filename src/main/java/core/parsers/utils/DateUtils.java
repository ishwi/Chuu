package core.parsers.utils;

import dao.entities.TriFunction;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DateUtils {


    private static final DateTimeFormatter[] dateTimeFormatters;


    static {
        Function<String, DateTimeFormatter> build = s ->
                new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(s)
                        .toFormatter(Locale.UK);
        String[] dayFormats = new String[]{"d, E, EEE"};
        String[] monthFormats = new String[]{"M", "LLL", "LLLL"};
        String[] yearFormats = new String[]{"yyyy", "y"};
        String[][] formats = new String[][]{dayFormats, monthFormats, yearFormats};
        List<String> flattenFormats = List.of(".", "/", " ");
        TriFunction<String, String, String, String> stringBinaryOperator = (sep, s, s2) -> "[" + s + "][" + sep + "][" + s2 + "]";
        TriFunction<List<String>, String, String, List<String>> function = (separatorList, a1, a2) -> {
            ArrayList<String> string = new ArrayList<>();
            separatorList.forEach(x -> string.add(stringBinaryOperator.apply(x, a1, a2)));
            return string;
        };
        Set<String> order = new ListOrderedSet<>();
        order.add("yyyy/M/d");
        order.addAll(Arrays.asList(dayFormats));
        order.addAll(Arrays.asList(monthFormats));
        for (int i = 0, formatsLength = formats.length; i < formatsLength - 1; i++) {
            String[] dominant = formats[i];
            for (String s : dominant) {
                List<String[]> firstPairs = Arrays.stream(formats).filter(x -> !Arrays.equals(x, dominant) && !Arrays.equals(x, formats[2])).collect(Collectors.toList());
                assert firstPairs.size() == 1;
                for (String[] secondItem : firstPairs) {
                    for (String string : secondItem) {
                        List<String> apply = function.apply(flattenFormats, s, string);
                        order.addAll(apply);
                    }
                }
            }
        }
        for (int i = 0, formatsLength = formats.length; i < formatsLength - 1; i++) {
            String[] dominant = formats[i];
            for (String s : dominant) {
                List<String[]> firstPairs = Arrays.stream(formats).filter(x -> Arrays.equals(x, formats[2])).collect(Collectors.toList());
                assert firstPairs.size() == 1;
                for (String[] secondItem : firstPairs) {
                    for (String string : secondItem) {
                        List<String> apply = function.apply(flattenFormats, s, string);
                        order.addAll(apply);
                    }
                }
            }
        }

        for (String[] dominant : formats) {
            for (String s : dominant) {
                List<String[]> firstPairs = Arrays.stream(formats).filter(x -> !Arrays.equals(x, dominant)).collect(Collectors.toList());
                assert firstPairs.size() == 2;
                for (int j = 0; j < firstPairs.size(); j++) {
                    String[] secondItem = firstPairs.get(j);
                    for (String string : secondItem) {
                        List<String> apply = function.apply(flattenFormats, s, string);
                        for (int k = 0; k < apply.size(); k++) {
                            List<String[]> remainingPair = firstPairs.stream().filter(x -> !Arrays.equals(x, secondItem)).collect(Collectors.toList());
                            assert remainingPair.size() == 1;
                            String[] thirdPairs = remainingPair.get(0);
                            for (String thirdPair : apply) {
                                for (String s2 : thirdPairs) {
                                    List<String> threeMatched = function.apply(flattenFormats, thirdPair, s2);
                                    order.addAll(threeMatched);
                                }
                            }
                        }
                    }
                }
            }

        }
//        map = order.stream().collect(Collectors.toMap(build, x -> x));
        dateTimeFormatters = order.stream().map(build).toArray(DateTimeFormatter[]::new);

    }

    BiFunction<String, DateTimeFormatter[], Optional<OffsetDateTime>> eval = (s, formatArray) ->
            Arrays.stream(formatArray).map(x -> {
                try {
                    TemporalAccessor parse = x.parse(s);
                    int year;
                    int month;
                    int day;
                    boolean previousFlag = false;
                    boolean possibleError = false;
                    try {
                        month = parse.get(ChronoField.MONTH_OF_YEAR);
                        if (month > LocalDate.now().getMonthValue()) {
                            previousFlag = true;
                        }
                    } catch (DateTimeException possibleFormatError) {
                        possibleError = true;
                        month = 1;
                    } catch (Exception exception) {
                        month = 1;
                    }

                    try {
                        year = parse.get(ChronoField.YEAR);
                    } catch (Exception ignored) {
                        if (previousFlag) {
                            year = Year.now().minus(1, ChronoUnit.YEARS).getValue();
                        } else {
                            year = Year.now().getValue();
                        }
                    }
                    try {
                        day = parse.get(ChronoField.DAY_OF_MONTH);
                        if (day > 12 && possibleError) {
                            return null;
                        }
                    } catch (Exception ignored) {
                        day = 1;
                    }
                    return LocalDate.of(year, month, day).atStartOfDay().atOffset(ZoneOffset.UTC);
                } catch (
                        DateTimeParseException ex) {
                    return null;
                }
            }).filter(Objects::nonNull).findFirst();

    public DateUtils() {
    }

    public DateParsed parseOnlyOne(String[] words) {
        String join = String.join(" ", words);
        if (join.isBlank()) {
            return null;
        }
        Optional<OffsetDateTime> apply = eval.apply(join.trim(), dateTimeFormatters);
        return new DateParsed(words, apply.orElse(null), OffsetDateTime.now());
    }

    public DateParsed parseString(String[] words) {
        String join = String.join(" ", words);
        String regex = "([\\w\\d./\\\\ ]+)\\s*-\\s*(\\s[\\w\\d./\\\\ ]+)";
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(join);
        if (matcher.matches()) {
            String from = matcher.group(1);
            Optional<OffsetDateTime> apply = eval.apply(from.trim(), dateTimeFormatters);

            String to = matcher.group(2);
            Optional<OffsetDateTime> apply2 = eval.apply(to.trim(), dateTimeFormatters);
            words = join.replaceAll(from.trim() + "\\s+-\\s+" + to.trim(), "").split("\\s+");
            if (words.length == 1 && words[0].isBlank()) {
                words = new String[]{};
            }
            return new DateParsed(words, apply.orElse(null), apply2.orElse(null));
        }
        return null;
    }

    public static class DateParsed {
        private final String[] remainingWords;
        private final OffsetDateTime from;
        private final OffsetDateTime to;

        DateParsed(String[] remainingWords, OffsetDateTime from, OffsetDateTime to) {
            this.remainingWords = remainingWords;
            this.from = from;
            this.to = to;
        }

        public String[] getRemainingWords() {
            return remainingWords;
        }

        public OffsetDateTime getFrom() {
            return from;
        }

        public OffsetDateTime getTo() {
            return to;
        }
    }
}

