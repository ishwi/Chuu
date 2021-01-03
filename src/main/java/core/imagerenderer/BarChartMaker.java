package core.imagerenderer;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;

public class BarChartMaker {
    private BarChartMaker() {

    }

    public static BufferedImage makeBarChart(Map<LocalDate, Integer> dates) {
        BufferedImage bufferedImage = new BufferedImage(1200, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        Color color = GraphicUtils.makeMoreTransparent(Color.BLACK, 0.0f);
        CategoryChart chart = new CategoryChartBuilder()
                .theme(Styler.ChartTheme.GGPlot2)
                .xAxisTitle("Date").yAxisTitle("Scrobble Count")
                .width(1200).height(600).build();
        chart.getStyler().setBaseFont(chart.getStyler().getBaseFont().deriveFont(Font.BOLD, 32))
                .setChartTitleFont(chart.getStyler().getBaseFont());
        chart.getStyler()
                .setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar)
                .setDatePattern("d MMM")
                .setAxisTitleFont(chart.getStyler().getBaseFont())
                .setAxisTicksLineVisible(false)
                .setYAxisTickMarkSpacingHint(50)
                .setAxisTickLabelsFont(chart.getStyler().getBaseFont())
                .setAxisTickLabelsColor(Color.white)
                .setPlotGridLinesVisible(false)
                .setTimezone(TimeZone.getTimeZone(ZoneOffset.UTC));

        chart.getStyler().setLegendVisible(false)
                .setChartBackgroundColor(color)
                .setChartBackgroundColor(color)
                .setAnnotationsFontColor(Color.white)
                .setPlotBorderColor(color)
                .setLegendPosition(Styler.LegendPosition.InsideN).setHasAnnotations(true)
                .setShowTotalAnnotations(true)
                .setPlotBackgroundColor(color)
                .setXAxisTitleColor(Color.white)
                .setYAxisTitleColor(Color.white);
        final List<Date> xAxis = new ArrayList<>(dates.size());
        List<Integer> valueList = new ArrayList<>(dates.size());
        dates.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(
                x -> {
                    xAxis.add(Date.from(x.getKey().atStartOfDay().toInstant(ZoneOffset.UTC)));

                    valueList.add(x.getValue());
                }
        );
        chart.addSeries("Values", xAxis, valueList);

        chart.paint(g, 1200, 600);
        return bufferedImage;
    }
}
