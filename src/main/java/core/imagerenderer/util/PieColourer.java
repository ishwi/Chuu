package core.imagerenderer.util;


import org.knowm.xchart.PieChart;

import java.awt.*;

public abstract class PieColourer {

    public abstract Color getBackGroundColor();

    public abstract Color getTitleColour();

    public abstract Color getAnnotationColour();

    public abstract Color getBesselColour();

    public abstract Color[] setPieSeriesColour();

    public abstract Color setIndividualColour(int index, int totalCount);

    public abstract void configChart(PieChart pieChart);

}
