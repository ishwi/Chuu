package core.commands;

import core.apis.last.chartentities.PreComputedChartEntity;
import core.imagerenderer.GraphicUtils;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PreComputedByColor extends PreComputedChartEntity {

    public PreComputedByColor(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(inner, image, isDarkToWhite);
    }

    @Override
    public int compareTo(@NotNull PreComputedChartEntity o) {
        BufferedImage image = this.getImage();
        BufferedImage image1 = o.getImage();
        if (image == null && image1 == null)
            return 0;
        if (image == null) {
            return -1;
        }
        if (image1 == null) {
            return +1;
        }
        Color thisColor = GraphicUtils.averageColor(image);
        Color externalColor = GraphicUtils.averageColor(image1);
        if (isDarkToWhite()) {
            return -Integer.compare(externalColor.getRGB(), thisColor.getRGB());
        } else {
            return Integer.compare(externalColor.getRGB(), thisColor.getRGB());
        }
    }
}
