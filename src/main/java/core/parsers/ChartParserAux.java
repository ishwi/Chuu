package core.parsers;

import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.DateUtils;
import dao.entities.NaturalTimeFrameEnum;
import dao.entities.TimeFrameEnum;
import javacutils.Pair;

import javax.annotation.Nullable;
import java.awt.*;
import java.time.Year;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static core.parsers.Parser.filterMessage;

public class ChartParserAux {
    public static final Pattern chartSizePattern = Pattern.compile("\\d+[xX]\\d+");
    private static final Pattern pattern = Pattern.compile("(:?[yqsmwad]|(:?(:?day|daily)?)|(:?year(:?ly)?|month(:?ly)?|quarter(:?ly)?|semester(:?ly)?|week(:?ly)?|alltime|all))", Pattern.CASE_INSENSITIVE);
    private static final Pattern naturalPattern = Pattern.compile("(:?[yqsmwadh']|(:?year(:?ly)?(:?s)?(:?lies)?|month(:?ly)?(:?s)?(:?lies)?|quarter(:?ly)?(:?s)?(:?lies)?|semester(:?ly)?(:?s)?(:?lies)?|week(:?ly)?(:?s)?(:?lies)?|alltime|all|dai(:?ly)?(:?lies)?|day(:?s)?|" +
                                                                  "hour(:?ly)?(:?s)?|min(:?ute)?(:?s)?|sec(:?ond)?(:?s)?|''))", Pattern.CASE_INSENSITIVE);

    private static final Pattern naturalPatternWithCount = Pattern.compile("(:?(?<![\\w\\d])[yqsmwadh'](?![\\w\\d]+)|(:?year(:?ly)?(:?s)?(:?lies)?|month(:?ly)?(:?s)?(:?lies)?|quarter(:?ly)?(:?s)?(:?lies)?|semester(:?ly)?(:?s)?(:?lies)?|week(:?ly)?(:?s)?(:?lies)?|alltime|all|dai(:?ly)?(:?lies)?|day(:?s)?|" +
                                                                           "hour(:?ly)?(:?s)?|min(:?ute)?(:?s)?|sec(:?ond)?(:?s)?|''))", Pattern.CASE_INSENSITIVE);
    private static final Pattern nonPermissivePattern = Pattern.compile("[yqsmwd]", Pattern.CASE_INSENSITIVE);
    private final boolean permissive;
    private String[] message;

    public ChartParserAux(String[] message) {
        this(message, true);
    }

    ChartParserAux(String[] message, boolean permissive) {
        this.message = message;
        this.permissive = permissive;
    }

    public static int getYearFromDecade(int decade) {
        int value = Year.now().getValue();
        if (decade > 100) {
            return decade;
        }
        int year;
        if (decade <= value % 100) {
            year = 2000 + decade;
        } else {
            year = 1900 + decade;
        }
        return year;
    }

    public static Point processString(String chartSize) throws InvalidChartValuesException {
        String[] sizes = chartSize.split("[xX]");
        String x = sizes[0];
        String y = sizes[1];
        if (x.equals("0") || y.equals("0")) {
            throw new InvalidChartValuesException(x);
        }
        int x1 = Integer.parseInt(x);
        int y1 = Integer.parseInt(y);
        if (x1 * y1 > 400) {
            throw new InvalidChartValuesException(x);
        }
        return new Point(x1, y1);
    }

    public String[] getMessage() {
        return message;
    }

    public CustomTimeFrame parseCustomTimeFrame(TimeFrameEnum defaultTimeFrame) throws InvalidDateException {

        String[] ogMessage = message;
        Pair<String[], Long> unitCount = filterMessage(message, ParserAux.digitMatcher.asMatchPredicate(), Long::parseLong, 1L);
        message = unitCount.first;
        long count = unitCount.second;
        String words = String.join(" ", message);
        Matcher matcher = naturalPattern.matcher(words);
        if (matcher.matches()) {
            String natural = matcher.group(0);
            String permissiveString = String.valueOf(natural.charAt(0));
            if (List.of("hour", "hourly", "hours").contains(natural)) {
                permissiveString = "h";
            } else if (List.of("min", "mins", "minutes", "minute", "'").contains(natural)) {
                permissiveString = "min";
            } else if (List.of("sec", "second", "seconds", "''", "secs").contains(natural)) {
                permissiveString = "sec";
            }
            NaturalTimeFrameEnum naturalTimeFrameEnum = NaturalTimeFrameEnum.get(permissiveString);
            message = words.replaceAll(natural, "").split("\\s+");
            if (message.length == 1 && message[0].isBlank()) {
                message = new String[]{};
            }
            if (count == 1 && !EnumSet.of(NaturalTimeFrameEnum.MINUTE, NaturalTimeFrameEnum.SECOND, NaturalTimeFrameEnum.HOUR).contains(naturalTimeFrameEnum)) {
                return new CustomTimeFrame(TimeFrameEnum.fromCompletePeriod(naturalTimeFrameEnum.toApiFormat()));
            }
            return new CustomTimeFrame(naturalTimeFrameEnum, count);
        } else {
            message = ogMessage;
            DateUtils dateUtils = new DateUtils();
            DateUtils.DateParsed dateParsed = dateUtils.parseString(message);
            if (dateParsed == null || dateParsed.from() == null) {
                dateParsed = dateUtils.parseOnlyOne(message);
            }
            if (dateParsed == null || dateParsed.from() == null) {
                return new CustomTimeFrame(defaultTimeFrame);
            }
            message = dateParsed.remainingWords();
            if (dateParsed.from().isAfter(dateParsed.to())) {
                throw new InvalidDateException();
            }
            return new CustomTimeFrame(dateParsed.from(), dateParsed.to());
        }
    }

    NaturalTimeFrameEnum parseNaturalTimeFrame() {
        return getNaturalTimeFrameEnum(null);
    }

    private NaturalTimeFrameEnum getNaturalTimeFrameEnum(NaturalTimeFrameEnum timeFrame) {
        Stream<String> secondStream = Arrays.stream(message).filter(s -> naturalPattern.matcher(s).matches());
        Optional<String> opt2 = secondStream.findAny();
        if (opt2.isPresent()) {
            String permissiveString = String.valueOf(opt2.get().charAt(0));
            if (List.of("hour", "hourly", "hours").contains(opt2.get())) {
                permissiveString = "h";
            } else if (List.of("min", "mins", "minutes", "minute", "'").contains(opt2.get())) {
                permissiveString = "min";
            } else if (List.of("sec", "second", "seconds", "''", "secs").contains(opt2.get())) {
                permissiveString = "sec";
            }
            timeFrame = NaturalTimeFrameEnum.get(permissiveString);
            message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);
        }
        return timeFrame;
    }

    NaturalTimeFrameEnum parseNaturalTimeFrame(NaturalTimeFrameEnum defaultTimeFrame) {
        return getNaturalTimeFrameEnum(defaultTimeFrame);
    }

    TimeFrameEnum parseTimeframe(TimeFrameEnum defaultTimeFrame) {
        TimeFrameEnum timeFrame = defaultTimeFrame;
        Stream<String> secondStream = Arrays.stream(message).filter(s ->
                !permissive
                ? nonPermissivePattern.matcher(s).matches()
                : pattern.matcher(s).matches());
        Optional<String> opt2 = secondStream.findAny();
        if (opt2.isPresent()) {
            String permissiveString = !permissive ? opt2.get() : String.valueOf(opt2.get().charAt(0));
            timeFrame = TimeFrameEnum.get(permissiveString);
            message = Arrays.stream(message).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);
        }
        return timeFrame;
    }

    Year parseYear() {
        Year year = Year.now();
        Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches("\\d{4}"));
        Optional<String> opt1 = firstStream.findAny();
        if (opt1.isPresent()) {
            year = Year.of(Integer.parseInt(opt1.get()));
            message = Arrays.stream(message).filter(s -> !s.equals(opt1.get().trim())).toArray(String[]::new);

        }
        return year;
    }

    private int getDecade(int year) {
        return year < 2000 ? (year / 10 * 10) : (year / 10 * 10);
    }

    int parseDecade() {
        Optional<String> any = Arrays.stream(message).filter(s -> s.matches("(\\d{2})(?:s|'s)?")).findAny();
        if (any.isPresent()) {

            String s1 = any.get();
            String decade = s1.split("(s|'s)")[0];
            message = Arrays.stream(message).filter(s -> !s.equals(s1.trim())).toArray(String[]::new);
            return getYearFromDecade(Integer.parseInt(decade));
        }
        Year year = parseYear();
        return getDecade(year.getValue());
    }

    @Nullable
    public Point getChartSize() throws InvalidChartValuesException {

        Optional<String> opt = Arrays.stream(message).filter(s -> chartSizePattern.matcher(s).matches()).findAny();
        if (opt.isPresent()) {
            message = Arrays.stream(message).filter(s -> !s.equals(opt.get())).toArray(String[]::new);
            return processString(opt.get());
        }
        return null;
    }
}
