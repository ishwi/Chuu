package core.apis.last.chartentities;

import core.imagerenderer.GraphicUtils;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PreComputedByBrightness extends PreComputedChartEntity {
    public PreComputedByBrightness(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(inner, image, isDarkToWhite);
    }

    @Override
    public int compareTo(@NotNull PreComputedChartEntity o) {
        Color thisColor = this.getColorToCompare();
        Color colorToCompare = o.getColorToCompare();
        if (thisColor == null && colorToCompare == null)
            return 0;
        if (thisColor == null) {
            return 1;
        }
        if (colorToCompare == null) {
            return -1;
        }
        if (GraphicUtils.isWhiter(thisColor, colorToCompare)) return !isDarkToWhite() ? -1 : +1;
        else return isDarkToWhite() ? -1 : +1;


    }
}
