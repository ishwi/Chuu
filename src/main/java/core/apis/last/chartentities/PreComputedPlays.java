package core.apis.last.chartentities;

import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class PreComputedPlays extends PreComputedChartEntity {
    public PreComputedPlays(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite, ImageComparison comparison) {
        super(inner, image, isDarkToWhite, comparison);
    }

    public PreComputedPlays(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(inner, image, isDarkToWhite);
    }

    @Override
    public int compareTo(@NotNull PreComputedChartEntity o) {
        return -Integer.compare(this.getPlays(), o.getPlays());
    }
}
