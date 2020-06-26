package core.parsers;

import core.parsers.params.DateParameters;
import dao.ChuuService;
import dao.entities.NaturalTimeFrameEnum;
import dao.entities.TriFunction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
import java.util.stream.Collectors;

public class DateParser extends DaoParser<DateParameters> {
    private static final BiFunction<String, DateTimeFormatter[], Optional<OffsetDateTime>> eval = (s, formatArray) ->
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
    private static final DateTimeFormatter[] dateTimeFormatters;

    static {
        Function<String, DateTimeFormatter> build = s ->
                new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(s)
                        .toFormatter(Locale.UK);
        String[] dayFormats = new String[]{"dd"};
        String[] monthFormats = new String[]{"MM", "MMM", "MMMM"};
        String[] yearFormats = new String[]{"yyyy", "yy"};
        String[][] formats = new String[][]{dayFormats, monthFormats, yearFormats};
        List<String> flattenFormats = List.of("-", "/", " ");
        TriFunction<String, String, String, String> stringBinaryOperator = (sep, s, s2) -> "[" + s + "][" + sep + "][" + s2 + "]";
        TriFunction<List<String>, String, String, List<String>> function = (separatorList, a1, a2) -> {
            ArrayList<String> string = new ArrayList<>();
            separatorList.forEach(x -> string.add(stringBinaryOperator.apply(x, a1, a2)));
            return string;
        };
        Set<String> order = new ListOrderedSet<>();
        order.add("yyyy/MM/dd");
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
        dateTimeFormatters = order.stream().map(build).toArray(DateTimeFormatter[]::new);


    }

    public DateParser(ChuuService service) {
        super(service);

    }

    @Override
    protected DateParameters parseLogic(MessageReceivedEvent e, String[] words) {

        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e);
        words = parserAux.getMessage();

        ChartParserAux chartParserAux = new ChartParserAux(words);
        NaturalTimeFrameEnum naturalTimeFrameEnum = chartParserAux.parseNaturalTimeFrame();
        words = chartParserAux.getMessage();
        int count = 1;
        OffsetDateTime localDate;

        if (naturalTimeFrameEnum != null) {
            if (words.length == 1) {
                try {
                    count = Integer.parseInt(words[0]);
                } catch (NumberFormatException ignored) {
                    //It wasn't a number
                }
            }
            localDate = naturalTimeFrameEnum.toLocalDate(count).atOffset(OffsetDateTime.now().getOffset());
        } else {
            if (words.length == 0) {
                localDate = LocalDate.now().with(MonthDay.of(1, 1)).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            } else {
                String remaining = String.join(" ", words);
                remaining = remaining.replaceAll("[ ][ -/]+", " ");

                Optional<OffsetDateTime> apply = eval.apply(remaining, dateTimeFormatters);
                localDate = apply.orElse(null);
            }
        }
        if (localDate == null) {
            sendError(getErrorMessage(5), e);
            return null;
        } else {
            if (localDate.isAfter(LocalDateTime.now().atOffset(OffsetDateTime.now().getOffset()))) {
                sendError(getErrorMessage(6), e);
                return null;
            }
            return new DateParameters(e, sample, localDate);
        }

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *username* *time*** \n" +
                "\t time can be one of the following: \n" +
                "\t\t One of Year,Quarter,Month,All,Semester,Week,Day,Hour,Minute,Second with plural forms and abbreviations included followed or preceded by a number (number of periods)\n " +
                "\t\t A Date on a whole lot of different formats,if what you want doesnt match what the bot reads you can always default to `Year/Month/Day`\n" +
                "\tIf an username it's not provided it defaults to authors account, only ping and tag format (user#number)\n ";

    }

    @Override
    protected void setUpErrorMessages() {
        super.setUpErrorMessages();
        this.errorMessages.put(5, "The provided date couldn't be parsed.\nIf you are a having issues you can always default to `Day/Month/Year`");
        this.errorMessages.put(6, "The provided date is in the fut parsed.\nIf this date was not properly parsed you can always default to `Day/Month/Year`");

    }
}
