package core.commands.utils;

import core.imagerenderer.GraphicUtils;
import org.knowm.xchart.PieChart;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public record PieDoer(@Nullable String subtitle, String url, PieChart pieChart) {
    public BufferedImage fill() {
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        Font annotationsFont = pieChart.getStyler().getLabelsFont();
        pieChart.paint(g, 1000, 750);
        g.setFont(annotationsFont.deriveFont(11.0f));
        if (subtitle != null) {
            Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(subtitle, g);
            g.drawString(subtitle, 1000 - 10 - (int) stringBounds.getWidth(), 740 - 2);
        }
        GraphicUtils.drawImageInCorner(url, g);
        return bufferedImage;
    }
}
