/*
 * Java Color Thief
 * by Sven Woltmann, Fonpit AG
 *
 * https://www.androidpit.com
 * https://www.androidpit.de
 *
 * License
 * -------
 * Creative Commons Attribution 2.5 License:
 * http://creativecommons.org/licenses/by/2.5/
 *
 * Thanks
 * ------
 * Lokesh Dhakar - for the original Color Thief JavaScript version
 * available at http://lokeshdhakar.com/projects/color-thief/
 */

package core.imagerenderer.stealing.colorpicker;

import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;
import java.util.List;

public class ColorThiefCustom {
    private static final int DEFAULT_QUALITY = 10;
    private static final boolean DEFAULT_IGNORE_WHITE = true;

    /**
     * Gets the image's pixels via BufferedImage.getRaster().getDataBuffer(). Fast, but doesn't work
     * for all color models.
     *
     * @param sourceImage the source image
     * @param quality     1 is the highest quality settings. 10 is the default. There is a trade-off between
     *                    quality and speed. The bigger the number, the faster the palette generation but
     *                    the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     * @return an array of pixels (each an RGB int array)
     */
    public static Pair<Color, Color> getColor(BufferedImage sourceImage, int quality, boolean ignoreWhite) {
        Pair<List<Color>, Color> palette = getPalette(sourceImage, 5, quality, ignoreWhite);
        if (palette.getKey() == null) {
            return null;
        }
        Color color = palette.getLeft().get(0);

        return Pair.of(color, palette.getRight());
    }

    public static Pair<int[][], Color> getPalette(BufferedImage sourceImage, int colorCount) {
        Pair<MMCQ.CMap, Color> cmap = getColorMap(sourceImage, colorCount);
        if (cmap.getKey() == null) {
            return null;
        }
        return Pair.of(cmap.getKey().palette(), cmap.getRight());
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     *
     * @param sourceImage the source image
     * @param colorCount  the size of the palette; the number of colors returned
     * @param quality     1 is the highest quality settings. 10 is the default. There is a trade-off between
     *                    quality and speed. The bigger the number, the faster the palette generation but
     *                    the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     * @return the palette as array of RGB arrays
     * @throws IllegalArgumentException if quality is &lt; 1
     */
    public static Pair<List<Color>, Color> getPalette(
            BufferedImage sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        Pair<MMCQ.CMap, Color> cmap = getColorMap(sourceImage, colorCount, quality, ignoreWhite);
        if (cmap.getKey() == null) {
            return Pair.of(null, cmap.getRight());
        }
        int[][] palette = cmap.getKey().palette();
        List<Color> collect = Arrays.stream(palette).map(ints -> new Color(Math.min(255, ints[0]), Math.min(255, ints[1]), Math.min(255, ints[2]))).toList();
        return Pair.of(collect, cmap.getRight());
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     *
     * @param sourceImage the source image
     * @param colorCount  the size of the palette; the number of colors returned (minimum 2, maximum 256)
     * @return the color map
     */
    public static Pair<MMCQ.CMap, Color> getColorMap(BufferedImage sourceImage, int colorCount) {
        return getColorMap(sourceImage, colorCount, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    public static Pair<MMCQ.CMap, Color> getColorMap(
            BufferedImage sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        if (colorCount < 2 || colorCount > 256) {
            throw new IllegalArgumentException("Specified colorCount must be between 2 and 256.");
        }
        if (quality < 1) {
            throw new IllegalArgumentException("Specified quality should be greater then 0.");
        }

        Pair<int[][], Color> returnValue = switch (sourceImage.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR -> getPixelsFast(sourceImage, quality, ignoreWhite);
            default -> getPixelsSlow(sourceImage, quality, ignoreWhite);
        };

        // Send array to quantize function which clusters values using median cut algorithm
        return Pair.of(MMCQ.quantize(returnValue.getLeft(), colorCount), returnValue.getRight());
    }

    private static Pair<int[][], Color> getPixelsFast(
            BufferedImage sourceImage,
            int quality,
            boolean ignoreWhite) {
        DataBufferByte imageData = (DataBufferByte) sourceImage.getRaster().getDataBuffer();
        byte[] pixels = imageData.getData();
        int pixelCount = sourceImage.getWidth() * sourceImage.getHeight();

        int colorDepth;
        int type = sourceImage.getType();
        colorDepth = switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR -> 3;
            case BufferedImage.TYPE_4BYTE_ABGR -> 4;
            default -> throw new IllegalArgumentException("Unhandled type: " + type);
        };

        int expectedDataLength = pixelCount * colorDepth;
        if (expectedDataLength != pixels.length) {
            throw new IllegalArgumentException(
                    "(expectedDataLength = " + expectedDataLength + ") != (pixels.length = "
                            + pixels.length + ")");
        }

        // Store the RGB values in an array format suitable for quantize function

        // numRegardedPixels must be rounded up to avoid an ArrayIndexOutOfBoundsException if all
        // pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;
        int[][] pixelArray = new int[numRegardedPixels][];
        int offset, r, g, b, a;
        long sumr = 0, sumg = 0, sumb = 0;

        // Do the switch outside of the loop, that's much faster
        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR:
                for (int i = 0; i < pixelCount; i += quality) {
                    offset = i * 3;
                    b = pixels[offset] & 0xFF;
                    g = pixels[offset + 1] & 0xFF;
                    r = pixels[offset + 2] & 0xFF;

                    sumr += r;
                    sumg += g;
                    sumb += b;

                    // If pixel is not white
                    if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                        pixelArray[numUsedPixels] = new int[]{r, g, b};
                        numUsedPixels++;
                    }
                }
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
                for (int i = 0; i < pixelCount; i += quality) {
                    offset = i * 4;
                    a = pixels[offset] & 0xFF;
                    b = pixels[offset + 1] & 0xFF;
                    g = pixels[offset + 2] & 0xFF;
                    r = pixels[offset + 3] & 0xFF;

                    sumr += r;
                    sumg += g;
                    sumb += b;

                    // If pixel is mostly opaque and not white
                    if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                        pixelArray[numUsedPixels] = new int[]{r, g, b};
                        numUsedPixels++;
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unhandled type: " + type);
        }

        // Remove unused pixels from the array
        Color averageColor = new Color((int) sumr / pixelCount, (int) sumg / pixelCount, (int) sumb / pixelCount);
        return Pair.of(Arrays.copyOfRange(pixelArray, 0, numUsedPixels), averageColor);
    }

    /**
     * Gets the image's pixels via BufferedImage.getRGB(..). Slow, but the fast method doesn't work
     * for all color models.
     *
     * @param sourceImage the source image
     * @param quality     1 is the highest quality settings. 10 is the default. There is a trade-off between
     *                    quality and speed. The bigger the number, the faster the palette generation but
     *                    the greater the likelihood that colors will be missed.
     * @param ignoreWhite if <code>true</code>, white pixels are ignored
     * @return an array of pixels (each an RGB int array)
     */
    private static Pair<int[][], Color> getPixelsSlow(
            BufferedImage sourceImage,
            int quality,
            boolean ignoreWhite) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        int pixelCount = width * height;

        // numRegardedPixels must be rounded up to avoid an ArrayIndexOutOfBoundsException if all
        // pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;

        int[][] res = new int[numRegardedPixels][];
        int r, g, b;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int i = 0; i < pixelCount; i += quality) {
            int row = i / width;
            int col = i % width;
            int rgb = sourceImage.getRGB(col, row);

            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = (rgb) & 0xFF;

            sumr += r;
            sumg += g;
            sumb += b;

            if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                res[numUsedPixels] = new int[]{r, g, b};
                numUsedPixels++;
            }
        }
        Color averageColor = new Color((int) sumr / pixelCount, (int) sumg / pixelCount, (int) sumb / pixelCount);
        return Pair.of(Arrays.copyOfRange(res, 0, numUsedPixels), averageColor);
    }
}
