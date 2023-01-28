package core.apis.last.entities.chartentities;

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
        return switch (getComparisonType()) {
            case ONLY_AVERAGE -> compareTwoByColor(this.getAverageColor(), o.getAverageColor());
            case AVERAGE_AND_DOMINANT, AVERAGE_AND_DOMINANT_PALETTE, ONLY_DOMINANT, ONLY_DOMINANT_PALETTE ->
                    compareTwoByColor(this.getDominantColor() == null || this.getDominantColor().isEmpty() ? null : this.getDominantColor().get(0),
                            o.getDominantColor() == null || o.getDominantColor().isEmpty() ? null : o.getDominantColor().get(0));
        };
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
