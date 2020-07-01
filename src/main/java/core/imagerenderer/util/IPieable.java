package core.imagerenderer.util;

import core.commands.CommandUtil;
import core.imagerenderer.GraphicUtils;
import core.parsers.params.CommandParameters;
import org.apache.commons.lang3.tuple.Pair;
import org.beryx.awt.color.ColorFactory;
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
import java.util.stream.Collectors;

import static core.imagerenderer.GraphicUtils.getBetter;
import static core.imagerenderer.GraphicUtils.getBetterPie;

public interface IPieable<T, Y extends CommandParameters> {


    default PieChart buildPie() {
        PieChart pieChart =
                new PieChartBuilder()
                        .width(1000)
                        .height(750).theme(Styler.ChartTheme.GGPlot2)
                        .build();
        List<Color> colors = GraphicUtils.palettes.get(CommandUtil.rand.nextInt(GraphicUtils.palettes.size()));
        Color[] seriesColors = colors.toArray(Color[]::new);
        Pair<Color, Color> betterPie = getBetterPie(seriesColors);

        Color better = getBetter(betterPie.getLeft());
        PieStyler styler = pieChart.getStyler();
        styler.setLegendVisible(false);
        styler.setAnnotationDistance(1.15);
        styler.setPlotContentSize(0.85);
        styler.setCircular(true);
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        styler.setDrawAllAnnotations(true);
        styler.setClockwiseDirectionType(CommandUtil.rand.nextBoolean() ? PieStyler.ClockwiseDirectionType.CLOCKWISE : PieStyler.ClockwiseDirectionType.COUNTER_CLOCKWISE);
        styler.setStartAngleInDegrees(90);
        styler.setChartFontColor(better);
        styler.setPlotBackgroundColor(betterPie.getLeft());
        styler.setCursorFontColor(better);
        styler.setAnnotationsFontColor(better);
        styler.setPlotBorderVisible(false);
        styler.setStartAngleInDegrees(CommandUtil.rand.nextInt(360));
        styler.setChartTitleBoxBackgroundColor(betterPie.getRight());
        styler.setChartBackgroundColor(betterPie.getRight());
        styler.setSeriesColors(seriesColors);

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
