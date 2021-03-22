package core.imagerenderer.util.pie;

import core.commands.utils.CommandUtil;
import core.imagerenderer.GraphicUtils;
import org.beryx.awt.color.ColorFactory;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RandomPalette extends PieColourer {
    public static final List<List<Color>> palettes;

    static {
        List<String[]> palette = new ArrayList<>();
        palette.add(new String[]{"#ddf3f5", "a6dcef", "e36387", "f2aaaa", "cff6cf", "e5cfe5", "cfe5cf"});
        palette.add(new String[]{"142850", "27496d", "00909e", "dae1e7", "fbe6d4", "fecb89"});
        palette.add(new String[]{"f67280", "e23e57", "88304e", "522546", "311d3f", "355c7d"});
        palette.add(new String[]{"#648FFF", "785EF0", "DC267F", "FE6100", "FFB000", "B54325", "48B02A", "#522779"});
        palette.add(new String[]{"#332288", "#117733", "#44AA99", "#88CCEE", "#DDCC77", "#CC6677", "#AA4499", "#882255"});
        palette.add(new String[]{"#000000", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", "#D55E00", "#CC79A7"});
        palette.add(new String[]{"#CC79A7", "#bfeeb2", "#4c6452", "#c8bbca", "#b2fb7e", "#dbf674", "#ac88ee", "#36ad46", "#a43e3d", "#800c6b", "#3bcd92"});
        palette.add(new String[]{"#68FFE0", "#2c1efa", "#4bc6c6", "#fc3e00", "#41a873", "#44039b", "#3a5930", "#67db91", "#51c33f", "#277e1f", "#ab4761", "#4e8e93"});
        palette.add(new String[]{"#FFBC9F", "#846853", "#a0e3b2", "#23270d", "#1c261b", "#af293e", "#23f224", "#f1196c", "#272c81", "#5ac146"});
        palette.add(new String[]{"#FFFFFF", "#df66f1", "#646acb", "#3d1a4c", "#b1b259", "#bf0869", "#afdac2"});
        palette.add(new String[]{"#000000", "#5f2735", "#fd9f40", "#ca86d6", "#bdbade", "#37b6d6", "#fecaf1"});
        palette.add(new String[]{"#FF00BF", "#5586b3", "#b2c7e8", "#1e589f", "#9f1f97", "#8c0f7d", "#3f4474"});
        palette.add(new String[]{"#FFE47E", "#4720df", "#2baa71", "#6925eb", "#69c671", "#9ef4e6", "#ada630", "#e93dac"});
        palettes = palette.stream().map(x -> Arrays.stream(x).map(ColorFactory::valueOf).collect(Collectors.toList())).toList();
    }

    private final List<Color> chosenPalette;
    private final PieTheme chosenTheme;
    private final Color fontColor;

    public RandomPalette() {
        chosenPalette = palettes.get(CommandUtil.rand.nextInt(palettes.size()));
        chosenTheme = getBetterPie(chosenPalette.toArray(Color[]::new));
        fontColor = GraphicUtils.getBetter(chosenTheme.getBackgroundColor());
    }

    public static PieTheme getBetterPie(Color... color) {
        double accum = 0;
        for (Color col : color) {
            accum += 0.2126 * col.getRed() + 0.7152 * col.getGreen() + 0.0722 * col.getBlue();
        }
        double v = accum / color.length;
        if (v > 196) {
            return PieTheme.PINK_THEME;
        }
        if (v > 128) {
            return PieTheme.BLUE_THEME;
        }
        return PieTheme.DARK_THEME;
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
        return chosenPalette.toArray(Color[]::new);
    }

    @Override
    public Color setIndividualColour(int index, int totalCount) {
        return null;
    }

    @Override
    public void configChart(PieChart pieChart) {
    }
}
