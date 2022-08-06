package core.imagerenderer.util.pie;

import core.imagerenderer.util.bubble.StringFrequency;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

public class PieableListResultWrapper<T, Y extends CommandParameters> extends OptionalPie implements IPieableList<T, Y> {

    private final Function<T, String> keyMapper;
    private final ToLongFunction<T> valueMapper;
    Predicate<T> parted;

    public PieableListResultWrapper(Parser<Y> parser, Function<T, String> keyMapper, ToLongFunction<T> valueMapper) {

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

    public PieableListResultWrapper(Parser<Y> parser, Function<T, String> keyMapper, ToLongFunction<T> valueMapper, Predicate<T> parted) {
        super(parser);
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.parted = parted;
    }


    @Override
    public PieChart fillPie(PieChart chart, Y params, List<T> data) {
        IPieableList.fillListedSeries(chart,
                keyMapper,
                valueMapper,
                data);

        return chart;
    }

    @Override
    public List<StringFrequency> obtainFrequencies(List<T> data, Y params) {
        return data.stream().map(t -> new StringFrequency(keyMapper.apply(t), valueMapper.applyAsLong(t))).toList();
    }
}
