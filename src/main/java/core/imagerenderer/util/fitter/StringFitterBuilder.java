package core.imagerenderer.util.fitter;

import core.imagerenderer.GraphicUtils;

import java.awt.*;

public class StringFitterBuilder {


    private Font baseFont = GraphicUtils.NORMAL_FONT;
    private Font[] fallbacks = GraphicUtils.palletes;
    private StringFitter.FitStrategy fitStrategy = StringFitter.FitStrategy.WIDTH;
    private float startingSize;

    private float minSize = 8f;
    private int containerSize;
    private int style = Font.PLAIN;
    private int step = 2;

    public StringFitterBuilder(float startingSize, int containerSize) {
        this.startingSize = startingSize;
        this.containerSize = containerSize;
    }


    public StringFitter build() {
        return new StringFitter(baseFont, fallbacks, fitStrategy, startingSize, minSize, containerSize, style, step);
    }

    public Font getBaseFont() {
        return baseFont;
    }

    public StringFitterBuilder setBaseFont(Font baseFont) {
        this.baseFont = baseFont;
        return this;
    }

    public Font[] getFallbacks() {
        return fallbacks;
    }

    public StringFitterBuilder setFallbacks(Font[] fallbacks) {
        this.fallbacks = fallbacks;
        return this;

    }

    public StringFitter.FitStrategy getFitStrategy() {
        return fitStrategy;
    }

    public StringFitterBuilder setFitStrategy(StringFitter.FitStrategy fitStrategy) {
        this.fitStrategy = fitStrategy;
        return this;

    }

    public float getStartingSize() {
        return startingSize;
    }

    public StringFitterBuilder setStartingSize(float startingSize) {
        this.startingSize = startingSize;
        return this;

    }

    public float getMinSize() {
        return minSize;
    }

    public StringFitterBuilder setMinSize(float minSize) {
        this.minSize = minSize;
        return this;

    }

    public int getContainerSize() {
        return containerSize;
    }

    public StringFitterBuilder setContainerSize(int containerSize) {
        this.containerSize = containerSize;
        return this;

    }

    public int getStyle() {
        return style;
    }

    public StringFitterBuilder setStyle(int style) {
        this.style = style;
        return this;
    }

    public int getStep() {
        return step;
    }

    public StringFitterBuilder setStep(int step) {
        this.step = step;
        return this;
    }
}
