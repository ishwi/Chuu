package core.commands.util;

import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class PieableResultWrapper<T, Y extends CommandParameters> extends OptionalPie implements IPieable<T, Y> {

    private final Function<T, String> keyMapper;
    private final ToIntFunction<T> valueMapper;
    Predicate<T> parted;

    public PieableResultWrapper(Parser<Y> parser, Function<T, String> keyMapper, ToIntFunction<T> valueMapper) {

        this(parser, keyMapper, valueMapper, null);
        AtomicInteger acceptedCount = new AtomicInteger(0);

        this.parted = x -> {
            if (acceptedCount.get() < 13) {
                acceptedCount.incrementAndGet();
                return true;
            } else {
                return false;
            }
        };


    }

    public PieableResultWrapper(Parser<Y> parser, Function<T, String> keyMapper, ToIntFunction<T> valueMapper, Predicate<T> parted) {
        super(parser);
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.parted = parted;
    }


    @Override
    public PieChart fillPie(PieChart chart, Y params, List<T> data) {
        AtomicInteger acceptedCount = new AtomicInteger(0);
        fillSeries(chart,
                keyMapper,
                valueMapper,
                x -> {

                    if (parted.test(x)) {
                        acceptedCount.incrementAndGet();
                        return true;
                    } else {
                        return false;
                    }
                }, data);

        return chart;
    }
}
