package core.imagerenderer.util.pie;

import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class DefaultList {

    public static <T extends CommandParameters, Y> IPieableList<Y, T> fillPie(Function<Y, String> keyMapping, ToLongFunction<Y> valueMapping) {
        return (PieChart chart, T params, List<Y> data) -> {

            IPieableList.fillListedSeries(chart,
                    keyMapping,
                    valueMapping,
                    data);
            return chart;
        };
    }
}
