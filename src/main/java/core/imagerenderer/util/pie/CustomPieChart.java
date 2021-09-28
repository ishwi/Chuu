package core.imagerenderer.util.pie;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;

public class CustomPieChart extends PieChart {

    private final PieChartBuilder chartBuilder;
    private final PieColourer colourer;

    public CustomPieChart(PieChartBuilder chartBuilder, PieColourer colourer) {
        super(chartBuilder);
        this.chartBuilder = chartBuilder;
        this.colourer = colourer;
    }

    public PieChartBuilder getChartBuilder() {
        return chartBuilder;
    }

    public PieColourer getColourer() {
        return colourer;
    }

}
