package core.apis.last.entities.chartentities;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PreComputedByColor extends PreComputedChartEntity {

    public PreComputedByColor(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(inner, image, isDarkToWhite);
    }

    public PreComputedByColor(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite, ImageComparison comparison) {
        super(inner, image, isDarkToWhite, comparison);
    }

    @Override
    public int compareTo(@NotNull PreComputedChartEntity o) {
        return switch (getComparisonType()) {
            case ONLY_AVERAGE -> compareTwoByColor(this.getAverageColor(), o.getAverageColor());
            case AVERAGE_AND_DOMINANT, AVERAGE_AND_DOMINANT_PALETTE, ONLY_DOMINANT, ONLY_DOMINANT_PALETTE -> compareTwoByColor(this.getDominantColor() == null || this.getDominantColor().isEmpty() ? null : this.getDominantColor().get(0),
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
        float[] hsb1 = Color.RGBtoHSB(thisColor.getRed(), thisColor.getGreen(), thisColor.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(colorToCompare.getRed(), colorToCompare.getGreen(), colorToCompare.getBlue(), null);
        if (hsb1[0] < hsb2[0])
            return isDarkToWhite() ? 1 : -1;
        if (hsb1[0] > hsb2[0])
            return isDarkToWhite() ? -1 : 1;
        if (hsb1[1] < hsb2[1])
            return isDarkToWhite() ? 1 : -1;
        if (hsb1[1] > hsb2[1])
            return isDarkToWhite() ? -1 : 1;
        return
                isDarkToWhite() ? Float.compare(hsb2[2], hsb1[2]) : Float.compare(hsb1[2], hsb2[2]);
    }
}

