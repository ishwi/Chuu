package core.imagerenderer.util.fitter;

import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

public class StringFitter {
    private final Font baseFont;
    private final Font[] fallbacks;
    private final StringFitter.FitStrategy fitStrategy;
    private final float startingSize;
    private final float minSize;
    private final int containerSize;
    private final int fontStyle;
    private final int step;

    public StringFitter(Font baseFont, Font[] fallbacks, FitStrategy fitStrategy, float startingSize, float minSize, int containerSize, int fontStyle, int step) {
        this.baseFont = baseFont;
        this.fallbacks = fallbacks;
        this.fitStrategy = fitStrategy;
        this.startingSize = startingSize;
        this.minSize = minSize;
        this.containerSize = containerSize;
        this.fontStyle = fontStyle;
        this.step = step;
    }

    public FontMetadata getFontMetadata(Graphics2D g, String test) {
        return getFontMetadata(g, test, this.containerSize);
    }

    public FontMetadata getFontMetadata(Graphics2D g, String test, int containerOverride) {
        AttributedString result = new AttributedString(test);
        int length = test.length();
        int i = baseFont.canDisplayUpTo(test);

        // Can display at least a character
        int maxSize = 0;
        Font maxFont = baseFont;


        List<StringAtrributes> temp = new ArrayList<>();
        if (i != 0) {
            temp.add(new StringAtrributes(baseFont.deriveFont(fontStyle, startingSize), 0, i == -1 ? length : i));
            maxSize = i;
        }
        // Couldnt display the whole string
        Font[] fallbacks = this.fallbacks;
        if (i != -1) {
            boolean broke = false;
            boolean restarting = false;
            boolean restarted = false;
            while (i < length) {

                for (Font value : fallbacks) {
                    String continued = test.substring(i);
                    int j = value.canDisplayUpTo(continued);
                    // We didnt have a main font.
                    if ((j != 0) && i == 0) {
                        maxSize = j;
                        maxFont = value;
                        if (j == -1) {
                            i = length;
                            broke = true;
                        } else {
                            i = j;
                            restarting = true;
                        }
                        temp.add(new StringAtrributes(value, 0, i));

                        break;
                    } else if (j == -1) {
                        temp.add(new StringAtrributes(value, i, length));
                        maxFont = value;
                        broke = true;
                        break;
                    } else if (j != 0) {
                        if (j > maxSize) {
                            maxSize = j;
                            maxFont = value;
                        }
                        temp.add(new StringAtrributes(value, i, i + j));
                        i += j;
                        restarting = true;
                        break;
                    }
                }
                if (restarting) {
                    if (!restarted) {
                        restarted = true;
                        fallbacks = ArrayUtils.addFirst(fallbacks, baseFont);
                    }
                    restarting = false;
                } else if (!broke) {
                    // Didn't manage to write one character?
                    temp.add(new StringAtrributes(baseFont, i, ++i));
                    fallbacks = ArrayUtils.addFirst(fallbacks, baseFont);
                    restarted = true;
                } else {
                    break;
                }
            }
        }
        float sizeFont = startingSize;
        switch (fitStrategy) {
            case HEIGHT -> {
                while (g.getFontMetrics(maxFont.deriveFont(sizeFont)).getStringBounds(test, g).getHeight() >= containerOverride && sizeFont > minSize) {
                    sizeFont -= step;
                }
            }
            case WIDTH -> {
                while (g.getFontMetrics(maxFont.deriveFont(sizeFont)).stringWidth(test) > containerOverride && sizeFont > minSize) {
                    sizeFont -= step;
                }
            }
        }

        maxFont = maxFont.deriveFont(fontStyle, sizeFont);
        g.getFontMetrics(maxFont).getStringBounds(test, g);
        for (StringAtrributes t : temp) {
            result.addAttribute(TextAttribute.FONT, t.font.deriveFont(fontStyle, sizeFont), t.begginging, t.end);
        }
        Rectangle2D bounds = g.getFontMetrics(maxFont).getStringBounds(test, g);
        if (i == 0) {
            result.addAttribute(TextAttribute.FONT, baseFont.deriveFont(fontStyle, sizeFont), 0, length);
        }
        return new FontMetadata(result, bounds, maxFont);
    }

    public enum FitStrategy {
        HEIGHT, WIDTH
    }

    public static record StringAtrributes(Font font, int begginging, int end) {

    }

    public static record FontMetadata(AttributedString atrribute, Rectangle2D bounds, Font maxFont) {

    }
}
