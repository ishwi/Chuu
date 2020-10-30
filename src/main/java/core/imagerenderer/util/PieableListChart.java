package core.imagerenderer.util;

import core.apis.last.chartentities.UrlCapsule;
import core.imagerenderer.ChartLine;
import core.parsers.Parser;
import core.parsers.params.ChartParameters;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PieableListChart extends OptionalPie implements IPieableList<UrlCapsule, ChartParameters> {


    public PieableListChart(Parser<?> parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, ChartParameters params, List<UrlCapsule> data) {
        int total = data.stream().mapToInt(UrlCapsule::getChartValue).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        fillListedSeries(chart,
                x -> x.getLines().stream().map(ChartLine::getLine).collect(Collectors.joining(" - ")),
                UrlCapsule::getChartValue,
                x -> {
                    if (x.getPos() < 10 || (counter.get() < breakpoint && acceptedCount.get() < 15)) {
                        counter.addAndGet(x.getChartValue());
                        acceptedCount.incrementAndGet();
                        return true;
                    } else {
                        return false;
                    }
                }, data);

        return chart;
    }
}

