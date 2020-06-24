package core.imagerenderer.util;

import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.*;
import java.util.List;
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


    PieChart fillPie(PieChart chart, Y params, T data);

    void fillSeries(PieChart pieChart, Function<T, String> keyMapping, ToIntFunction<T> valueMapping, Predicate<T> partitioner, T data);

    default PieChart doPie(Y params, T data) {
        PieChart pieChart = buildPie();
        return fillPie(pieChart, params, data);

    }

    Map<Boolean, Map<String, Integer>> getData(T data, Function<T, String> keyMapping, ToIntFunction<T> valueMapping, Predicate<T> partitioner);

}
