package core.parsers;

import com.sun.istack.Nullable;
import core.parsers.exceptions.InvalidChartValuesException;
import dao.entities.TimeFrameEnum;

import java.awt.*;
import java.time.Year;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class ChartParserAux {
    private final static Pattern pattern = Pattern.compile("(:?[yqsmwa]|(:?year(:?ly)?|month(:?ly)?|quarter(:?ly)?|semester(:?ly)?|week(:?ly)?|alltime|all))");
    private final static Pattern nonPermissivePattern = Pattern.compile("[yqsmwa]");
    private final static Pattern chartSizePattern = Pattern.compile("\\d+[xX]\\d+");
    private final boolean permissive;
    private String[] message;

    ChartParserAux(String[] message) {
        this(message, true);
    }

    ChartParserAux(String[] message, boolean permissive) {
        this.message = message;
        this.permissive = permissive;
    }


    public String[] getMessage() {
        return message;
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


    String parseYear() {
        String year = Year.now().toString();
        Stream<String> firstStream = Arrays.stream(message).filter(s -> s.matches("\\d{4}"));
        Optional<String> opt1 = firstStream.findAny();
        if (opt1.isPresent()) {
            year = opt1.get();
            message = Arrays.stream(message).filter(s -> !s.equals(opt1.get().trim())).toArray(String[]::new);

        }
        return year;
    }

    @Nullable Point getChartSize() throws InvalidChartValuesException {

        Optional<String> opt = Arrays.stream(message).filter(s -> chartSizePattern.matcher(s).matches()).findAny();
        if (opt.isPresent()) {
            String x = (opt.get().split("[xX]")[0]);
            String y = opt.get().split("[xX]")[1];
            message = Arrays.stream(message).filter(s -> !s.equals(opt.get())).toArray(String[]::new);
            if (x.equals("0") || y.equals("0")) {
                throw new InvalidChartValuesException(x);
            }
            return new Point(Integer.parseInt(x), Integer.parseInt(y));
        }
        return null;

    }
}
