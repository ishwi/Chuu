package core.apis.last.chartentities;

import core.imagerenderer.ChartLine;
import core.imagerenderer.GraphicUtils;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class PreComputedChartEntity extends UrlCapsule implements Comparable<PreComputedChartEntity> {
    private final BufferedImage image;
    private final UrlCapsule inner;
    private final boolean isDarkToWhite;
    private final Color colorToCompare;


    protected PreComputedChartEntity(UrlCapsule inner, BufferedImage image, boolean isDarkToWhite) {
        super(null, inner.getPos(), inner.getArtistName(), inner.getAlbumName(), inner.getMbid());
        this.isDarkToWhite = isDarkToWhite;
        setPlays(inner.getPlays());
        this.inner = inner;
        this.image = image;
        this.colorToCompare = image == null ? null : GraphicUtils.averageColor(image);
    }

    public abstract int compareTo(@NotNull PreComputedChartEntity o);

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

    public Color getColorToCompare() {
        return colorToCompare;
    }

    ;
}
