package core.commands.util;

import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface IPieable<T, Y extends CommandParameters> {


    default PieChart buildPie() {
        PieChart pieChart =
                new PieChartBuilder()
                        .width(1000)
                        .height(750).theme(Styler.ChartTheme.GGPlot2)
                        .build();

        PieStyler styler = pieChart.getStyler();
        styler.setLegendVisible(false);
        styler.setAnnotationDistance(1.15);
        styler.setPlotContentSize(.7);
        styler.setCircular(true);
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        styler.setDrawAllAnnotations(true);
        styler.setStartAngleInDegrees(0);
        styler.setPlotBackgroundColor(Color.decode("#2c2f33"));
        styler.setCursorFontColor(Color.white);
        styler.setAnnotationsFontColor(Color.white);
        styler.setToolTipsAlwaysVisible(true);
        styler.setPlotBorderVisible(false);
        styler.setChartTitleBoxBackgroundColor(Color.decode("#23272a"));
        styler.setChartBackgroundColor(Color.decode("#23272a"));
        styler.setChartFontColor(Color.white);
        styler.getDefaultSeriesRenderStyle();
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        return pieChart;
    }


    PieChart fillPie(PieChart chart, Y params, List<T> data);

    default void fillSeries(PieChart pieChart, Function<T, String> keyMapping, ToIntFunction<T> valueMapping, Predicate<T> partitioner, List<T> data) {
        Map<Boolean, Map<String, Integer>> parted = getData(data, keyMapping, valueMapping, partitioner);
        AtomicInteger counter = new AtomicInteger(0);
        Map<String, Integer> entries = parted.get(true);
        int sum = parted.get(false).values().stream().mapToInt(i -> i).sum();
        entries.entrySet().stream().sorted((x, y) -> y.getValue().compareTo(x.getValue()))
                .forEachOrdered(entry -> {
                    int i = counter.incrementAndGet();
                    String key = entry.getKey();
                    try {
                        pieChart.addSeries(key.isBlank() ? UUID.randomUUID().toString() : key, entry.getValue());
                    } catch (IllegalArgumentException ex) {
                        pieChart.addSeries("\u200B".repeat(i) + key, entry.getValue());
                    }
                });
        if (sum != 0) {
            //To avoid having an artist called others and colliding bc no duplicates allowed
            pieChart.addSeries("Others\u200B", sum);
        }
    }

    default PieChart doPie(Y params, List<T> data) {
        PieChart pieChart = buildPie();
        return fillPie(pieChart, params, data);

    }

    default Map<Boolean, Map<String, Integer>> getData(List<T> data, Function<T, String> keyMapping, ToIntFunction<T> valueMapping, Predicate<T> partitioner) {
        Map<Boolean, Map<String, Integer>> parted = new HashMap<>(2);
        parted.put(true, new HashMap<>());
        parted.put(false, new HashMap<>());
        var entries = parted.get(true);
        data.forEach(x -> {
            if (partitioner.test(x)) {
                entries.put(keyMapping.apply(x), valueMapping.applyAsInt(x));
            } else {
                parted.get(false).put(keyMapping.apply(x), valueMapping.applyAsInt(x));
            }
        });
        return parted;
    }

}
