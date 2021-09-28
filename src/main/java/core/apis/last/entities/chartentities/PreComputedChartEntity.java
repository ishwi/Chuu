package core.apis.last.entities.chartentities;

import core.imagerenderer.ChartLine;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.stealing.colorpicker.ColorThiefCustom;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public abstract class PreComputedChartEntity extends UrlCapsule implements Comparable<PreComputedChartEntity> {
    private final BufferedImage image;
    private final UrlCapsule inner;
    private final boolean isDarkToWhite;
    private final ImageComparison comparisonType;
    private final List<Color> dominantColor;
    private final Color averageColor;

    protected PreComputedChartEntity(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        this(inner, image, isDarkToWhite, ImageComparison.ONLY_AVERAGE);
    }

    protected PreComputedChartEntity(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite, ImageComparison comparison) {
        super(null, inner.getPos(), inner.getArtistName(), inner.getAlbumName(), inner.getMbid());
        this.isDarkToWhite = isDarkToWhite;
        comparisonType = comparison;
        setPlays(inner.getPlays());
        this.inner = inner;
        this.image = image;
        Pair<List<Color>, Color> compareColor = getCompareColor(image);
        dominantColor = compareColor.getLeft();
        averageColor = compareColor.getRight();
    }

    public Pair<List<Color>, Color> getCompareColor(BufferedImage image) {
        if (image == null) return Pair.of(null, null);
        switch (this.comparisonType) {
            case ONLY_AVERAGE:
                return Pair.of(Collections.emptyList(), GraphicUtils.averageColor(image));

            case AVERAGE_AND_DOMINANT:
                Pair<Color, Color> color = ColorThiefCustom.getColor(image, 1, false);
                if (color == null) {
                    return null;
                }
                return Pair.of(color.getLeft() != null ? List.of(color.getLeft()) : Collections.emptyList(), color.getRight());
            case AVERAGE_AND_DOMINANT_PALETTE:
                return ColorThiefCustom.getPalette(image, 2, 1, false);

            case ONLY_DOMINANT:
            case ONLY_DOMINANT_PALETTE:
            default:
                throw new UnsupportedOperationException();

        }
    }

    public List<Color> getDominantColor() {
        return dominantColor;
    }

    public abstract int compareTo(@Nonnull PreComputedChartEntity o);

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public List<ChartLine> getLines() {
        return inner.getLines();
    }

    @Override
    public String toEmbedDisplay() {
        return inner.toEmbedDisplay();
    }

    @Override
    public String toChartString() {
        return inner.toChartString();
    }

    @Override
    public int getChartValue() {
        return inner.getChartValue();

    }

    public boolean isDarkToWhite() {
        return isDarkToWhite;
    }

    public Color getAverageColor() {
        return averageColor;
    }

    public ImageComparison getComparisonType() {
        return comparisonType;
    }

    public enum ImageComparison {
        ONLY_AVERAGE, AVERAGE_AND_DOMINANT, AVERAGE_AND_DOMINANT_PALETTE, ONLY_DOMINANT, ONLY_DOMINANT_PALETTE
    }
}
