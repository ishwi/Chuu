package core.apis.last.chartentities;

import core.imagerenderer.GraphicUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PreComputedByBrightness extends PreComputedChartEntity {
    public PreComputedByBrightness(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(inner, image, isDarkToWhite);
    }

    public PreComputedByBrightness(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite, ImageComparison comparison) {
        super(inner, image, isDarkToWhite, comparison);
    }

    @Override
    public int compareTo(@NotNull PreComputedChartEntity o) {
        switch (getComparisonType()) {
            case ONLY_AVERAGE:
                return compareTwoByColor(this.getAverageColor(), o.getAverageColor());
            case AVERAGE_AND_DOMINANT:
            case AVERAGE_AND_DOMINANT_PALETTE:
            case ONLY_DOMINANT:
            case ONLY_DOMINANT_PALETTE:
                return compareTwoByColor(this.getDominantColor() == null || this.getDominantColor().isEmpty() ? null : this.getDominantColor().get(0),
                        o.getDominantColor() == null || o.getDominantColor().isEmpty() ? null : o.getDominantColor().get(0));
            default:
                throw new UnsupportedOperationException();

        }
    }

    private int compareTwoByColor(Color thisColor, Color colorToCompare) {
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
