package core.imagerenderer.util.pie;

import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class DefaultList {

    public static <T extends CommandParameters, Y> IPieableList<Y, T> fillPie(Function<Y, String> keyMapping, ToIntFunction<Y> valueMapping) {
        return (PieChart chart, T params, List<Y> data) -> {

            int total = data.stream().mapToInt(valueMapping).sum();
            int breakpoint = (int) (0.75 * total);
            AtomicInteger counter = new AtomicInteger(0);
            AtomicInteger acceptedCount = new AtomicInteger(0);
            IPieableList.fillListedSeries(chart,
                    keyMapping,
                    valueMapping,
                    x -> {
                        if (acceptedCount.get() < 10 || (counter.get() < breakpoint && acceptedCount.get() < 15)) {
                            counter.addAndGet(valueMapping.applyAsInt(x));
                            acceptedCount.incrementAndGet();
                            return true;
                        } else {
                            return false;
                        }
                    }, data);
            return chart;
        };
    }
}
