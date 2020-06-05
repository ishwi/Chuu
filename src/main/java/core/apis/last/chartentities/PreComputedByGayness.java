package core.apis.last.chartentities;

import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PreComputedByGayness extends PreComputedByColor {
    private Color decidedCOlor;

    protected PreComputedByGayness(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(inner, image, isDarkToWhite);
    }

    public PreComputedByGayness(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite, ImageComparison comparison) {
        super(inner, image, isDarkToWhite, comparison);
    }

    public Color getDecidedCOlor() {
        return decidedCOlor;
    }

    public void setDecidedCOlor(Color decidedCOlor) {
        this.decidedCOlor = decidedCOlor;
    }
}
