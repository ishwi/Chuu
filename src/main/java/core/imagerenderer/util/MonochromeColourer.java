package core.imagerenderer.util;

import core.commands.CommandUtil;
import core.imagerenderer.GraphicUtils;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;

public class MonochromeColourer extends PieColourer {

    private final PieTheme chosenTheme;
    private final Color baseColour;
    private final Color fontColor;
    private final BiFunction<Color, Double, Color> transformer;

    public MonochromeColourer() {
        List<Color> colors = RandomPalette.palettes.get(CommandUtil.rand.nextInt(RandomPalette.palettes.size()));
        baseColour = colors.get(CommandUtil.rand.nextInt(colors.size()));
        chosenTheme = RandomPalette.getBetterPie(baseColour);
        fontColor = GraphicUtils.getBetter(chosenTheme.getBackgroundColor());
        transformer = GraphicUtils.getBetter(baseColour).equals(Color.black) ? GraphicUtils::slightlydarker : GraphicUtils::slightlybrighter;

    }


    @Override
    public Color getBackGroundColor() {
        return chosenTheme.getBackgroundColor();
    }

    @Override
    public Color getTitleColour() {
        return chosenTheme.getTitleColour();
    }

    @Override
    public Color getAnnotationColour() {
        return fontColor;
    }

    @Override
    public Color getBesselColour() {
        return chosenTheme.getBesselColour();
    }

    @Override
    public Color[] setPieSeriesColour() {
        return null;
    }

    @Override
    public Color setIndividualColour(int index, int totalCount) {
        int i = Math.max(1, totalCount);
        double base = Math.pow(0.33d, 1f / i);

        Color thisColor = new Color((baseColour).getRGB());
        for (int j = index; j < i; j++) {
            thisColor = transformer.apply(thisColor, base);
        }
        return thisColor;
    }

    @Override
    public void configChart(PieChart pieChart) {
        pieChart.getStyler().setStartAngleInDegrees(0);
    }

}
