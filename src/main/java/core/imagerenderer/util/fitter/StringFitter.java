package core.imagerenderer.util.fitter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (StringUtils.isBlank(test)) {
            return new FontMetadata(new AttributedString(test), new Rectangle(0, 0), baseFont);
        }


        Map<Font, Integer> lengths = new HashMap<>();

        AttributedString result = new AttributedString(test);
        int i = baseFont.canDisplayUpTo(test);

        // Can display at least a character
        int maxSize = 0;
        List<StringAttributes> temp = new ArrayList<>();
        if (i != 0) {
            temp.add(new StringAttributes(baseFont, 0, i == -1 ? test.length() : i));
            maxSize = i;
            lengths.put(baseFont, i == -1 ? (int) test.codePoints().count() : test.codePointCount(0, i));
        }
        // Couldn't display the whole string
        if (i != -1) {
            Font[] fontsToTest = this.fallbacks;

            int alreadyDrawn = test.codePointCount(0, maxSize);
            int stringIndex = maxSize;
            // All the codepoints on the string
            int[] array = test.codePoints().toArray();
            // Iterate the remaining codepoints;
            for (int j = alreadyDrawn, arrayLength = array.length; j < arrayLength; j++) {
                int codePoint = array[j];
                boolean set = false;
                for (Font value : fontsToTest) {
                    boolean canDisplayThisChar = value.canDisplay(codePoint);
                    if (canDisplayThisChar) {
                        int offset = Character.charCount(codePoint);
                        lengths.merge(value, offset, Integer::sum);
                        temp.add(new StringAttributes(value, stringIndex, stringIndex + offset));
                        stringIndex += offset;
                        set = true;
                        break;
                    }
                }
                if (!set) {
                    fontsToTest = ArrayUtils.addFirst(this.fallbacks, baseFont);
                    int offset = Character.charCount(codePoint);
                    temp.add(new StringAttributes(baseFont, stringIndex, stringIndex + offset));
                    stringIndex += offset;
                    lengths.merge(baseFont, offset, Integer::sum);
                }
            }

        }
        Font maxFont = lengths.entrySet().stream().sorted(Map.Entry.<Font, Integer>comparingByValue().reversed()).map(Map.Entry::getKey).findFirst()
                .orElse(baseFont)
                .deriveFont(fontStyle);

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
        maxFont = maxFont.deriveFont(sizeFont);
        g.getFontMetrics(maxFont).getStringBounds(test, g);
        for (StringAttributes t : temp) {
            result.addAttribute(TextAttribute.FONT, t.font.deriveFont(fontStyle, sizeFont), t.begginging, t.end);
        }
        Rectangle2D bounds = g.getFontMetrics(maxFont).getStringBounds(test, g);

        return new FontMetadata(result, bounds, maxFont);
    }

    public enum FitStrategy {
        HEIGHT, WIDTH
    }

    public record StringAttributes(Font font, int begginging, int end) {

    }

    public record FontMetadata(AttributedString atrribute, Rectangle2D bounds, Font maxFont) {

    }
}
