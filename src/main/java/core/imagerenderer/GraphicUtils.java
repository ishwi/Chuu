package core.imagerenderer;

import core.Chuu;
import core.imagerenderer.stealing.blur.GaussianFilter;
import core.imagerenderer.util.CIELab;
import core.imagerenderer.util.D;
import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.ReturnNowPlaying;
import dao.entities.WrapperReturnNowPlaying;
import org.imgscalr.Scalr;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class GraphicUtils {

    public static final BufferedImage noArtistImage;
    public static final Font NORMAL_FONT = new Font("Noto Sans", Font.PLAIN, 14);
    static final Random ran = new Random();
    static final File CacheDirectory;
    static final Font EMOJI_FONT_BACKUP = new Font("Segoe UI Emoji", Font.PLAIN, 14);
    static final Font NAMARE_FONT = new Font("Nirmala UI semilight", Font.PLAIN, 14);
    static final Font HEBREW_FONT = new Font("Heebo Light", Font.PLAIN, 14);
    static final Font ARABIC_FONT = new Font("Noto Sans Arabic Light", Font.PLAIN, 14);
    static final Font JAPANESE_FIRST = new Font("Noto Sans CJK JP Light", Font.PLAIN, 14);
    static final Font THAI_FONT = new Font("Noto Sans Thai Light", Font.PLAIN, 14);
    private static final Font JAPANESE_FONT = new Font("Yu Gothic", Font.PLAIN, 14);
    //private static final Font UNICODE_FONT = new Font("Sun-ExtA", Font.PLAIN, 14);
    private static final Font KOREAN_FONT = new Font("Malgun Gothic", Font.PLAIN, 14);
    private static final Font EMOJI_FONT = new Font("Symbola", Font.PLAIN, 14);
    public static final Font[] palletes = new Font[]{JAPANESE_FIRST, JAPANESE_FONT, KOREAN_FONT, EMOJI_FONT, EMOJI_FONT_BACKUP, HEBREW_FONT, NAMARE_FONT, ARABIC_FONT, THAI_FONT};
    private static final File walpepes;

    static {


        try (InputStream in = GraphicUtils.class.getResourceAsStream("/all.properties");
             InputStream in2 = WhoKnowsMaker.class.getResourceAsStream("/images/noArtistImage.png")) {
            if (in == null || in2 == null) {
                throw new IllegalStateException("/images/noArtistImage.png should exists under resources!!");
            }
            Properties properties = new Properties();
            properties.load(in);
            String cache_folder = properties.getProperty("CACHE_FOLDER");
            String path = properties.getProperty("WALLPAPER_FOLDER");
            walpepes = new File(path);
            CacheDirectory = new File(cache_folder);
            assert CacheDirectory.isDirectory();
            noArtistImage = ImageIO.read(in2);
        } catch (IOException e) {
            throw new IllegalStateException("/images/noArtistImage.png should exists under resources!!");
        }
    }

    private GraphicUtils() {
    }

    public static Color slightlydarker(Color color) {
        return slightlydarker(color, 0.85);
    }

    public static Color slightlydarker(Color color, double factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0),
                color.getAlpha());
    }

    public static Color slightlybrighter(Color color) {
        return slightlybrighter(color, 0.85);
    }

    public static Color slightlybrighter(Color color, double factor) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int) (1.0 / (1.0 - factor));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / factor), 255),
                Math.min((int) (g / factor), 255),
                Math.min((int) (b / factor), 255),
                alpha);
    }

    public static Color averageColor(BufferedImage bi) {
        long sumr = 0, sumg = 0, sumb = 0;
        int width = bi.getWidth();
        int height = bi.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = width * height;
        return new Color((int) sumr / num, (int) sumg / num, (int) sumb / num);
    }

    public static double getDistance(Color first, Color second) {

        float[] first1 = CIELab.getInstance().fromRGB(first.getColorComponents(null));
        float[] second1 = CIELab.getInstance().fromRGB(second.getColorComponents(null));

        return D.getTheFreakingDistance(first1, second1);

    }

    public static Font chooseFont(String string) {
        return getFont(string, NORMAL_FONT);
    }

    public static Font getFont(String test, Font startFont, Font... fonts) {
        Font font = startFont;
        int i = font.canDisplayUpTo(test);
        int maxI = i;
        if (i != -1) {
            for (Font value : fonts) {
                int i1 = value.canDisplayUpTo(test);
                if (i1 == -1) {
                    font = value;
                    break;
                }
                if (i1 > 0 && i1 > maxI) {
                    maxI = i1;
                    font = value;
                }
            }
        }
        return font;
    }

    public static Font getFont(String test, Font startFont) {
        return getFont(test, startFont, JAPANESE_FONT, KOREAN_FONT, EMOJI_FONT, EMOJI_FONT_BACKUP, HEBREW_FONT, NAMARE_FONT, ARABIC_FONT, THAI_FONT);
    }


    public static StringFitter.FontMetadata getFont(Graphics2D g, String test, Font startFont, float size, int maxWidth, int minFontSize, int fontStyle, Font... fonts) {
        AttributedString result = new AttributedString(test);
        int length = test.length();
        int i = startFont.canDisplayUpTo(test);

        // Can display at least a character
        int maxSize = 0;
        Font maxFont = startFont;


        List<StringFitter.StringAtrributes> temp = new ArrayList<>();
        if (i != 0) {
            result.addAttribute(TextAttribute.FONT, startFont.deriveFont(fontStyle, size), 0, length);
            maxSize = i;
        }
        // Couldnt display the whole string
        if (i != -1) {
            for (Font value : fonts) {
                String continued = test.substring(i);
                int j = value.canDisplayUpTo(continued);
                // We didnt have a main font.
                if ((j != 0) && i == 0) {
                    maxSize = j;
                    temp.add(new StringFitter.StringAtrributes(value, 0, length));
                    if (j == -1) {
                        i = j;
                        maxFont = value;
                        break;
                    }
                } else if (j == -1) {
                    temp.add(new StringFitter.StringAtrributes(value, i, length));
                    maxFont = value;
                    break;
                } else if (j != 0) {
                    if (j > maxSize) {
                        maxSize = j;
                        maxFont = value;
                    }
                    temp.add(new StringFitter.StringAtrributes(value, i, i + j));
                    i += j;
                }
            }
        }
        float sizeFont = size;
        while (g.getFontMetrics(maxFont.deriveFont(sizeFont)).stringWidth(test) > maxWidth && sizeFont > minFontSize) {
            sizeFont -= 2;
        }
        g.getFontMetrics(maxFont).getStringBounds(test, g);
        for (StringFitter.StringAtrributes t : temp) {
            result.addAttribute(TextAttribute.FONT, t.font().deriveFont(fontStyle, sizeFont), t.begginging(), t.end());
        }
        Rectangle2D bounds = g.getFontMetrics(maxFont).getStringBounds(test, g);
        if (i == 0) {
            result.addAttribute(TextAttribute.FONT, startFont.deriveFont(fontStyle, sizeFont), 0, length);
        }
        return new StringFitter.FontMetadata(result, bounds, maxFont.deriveFont(fontStyle, sizeFont));
    }


    public static void inserArtistImage(String urlImage, Graphics2D g) {
        BufferedImage read = GraphicUtils.getImage(urlImage);
        if (read != null) {
            BufferedImage image = Scalr.resize(read, 100, 100);
            g.drawImage(image, 10, 750 - 110, null);
        }

    }

    public static Graphics2D initArtistBackground(BufferedImage canvas, BufferedImage artistImage) {

        Graphics2D g = canvas.createGraphics();
        GraphicUtils.setQuality(g);
        if (artistImage == null) {
            return g;
        }
        g.drawImage(artistImage, 0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0, artistImage.getWidth(), artistImage
                .getHeight(), null);
        new GaussianFilter(75).filter(canvas, canvas);
        return g;
    }

    public static void setQuality(Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    }

    static Color getFontColorBackground(BufferedImage canvas) {
        int a = canvas.getRGB(0, 0);
        return new Color(a);
    }

    public static Color makeMoreTransparent(Color fontColor, float percentage) {
        float[] rgb2 = new float[3];
        fontColor.getRGBColorComponents(rgb2);
        return new Color(rgb2[0], rgb2[1], rgb2[2], percentage);

    }

    static Color getReadableColorBackgroundForFont(Color fontColor) {
        float[] rgb2 = new float[3];
        fontColor.getRGBColorComponents(rgb2);
        Color colorB1 = new Color(rgb2[0], rgb2[1], rgb2[2], 0.7f);
        return colorB1.darker().darker();
    }

    static Color getSurfaceColor(Color fontColor) {
        float[] rgb2 = new float[3];
        fontColor.getRGBColorComponents(rgb2);
        return new Color(rgb2[0], rgb2[1], rgb2[2], 0.5f).darker();
    }

    static BufferedImage getImageFromUrl(String urlImage, @Nullable BufferedImage replacement) {
        BufferedImage image = getImage(urlImage);
        if (image == null)
            return replacement;

        return image;

    }

    static void doChart(Graphics2D g, int x, int yCounter, int width, int height,
                        int maxRows, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage
                                lastFmLogo, Font font) {
        doChart(g, x, yCounter, width, height, maxRows, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, true, font);
    }

    public static void doChart(Graphics2D g, int x, int yCounter, int width, int rowHeight,
                               int maxRows, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage
                                       lastFmLogo, boolean doNumber, Font font) {
        doChart(g, x, yCounter, width, rowHeight, maxRows, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, doNumber, font, 0);
    }

    public static void doChart(Graphics2D g, int x, int yCounter, int width, int rowHeight,
                               int maxRows, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage
                                       lastFmLogo, boolean doNumber, Font font, int phase) {

        Font ogFont = g.getFont();
        Color ogColor = g.getColor();
        g.setColor(colorB1.brighter());
        g.fillRect(x, yCounter, width, rowHeight * maxRows);
        g.setColor(colorB);

        int rowContent = (int) (rowHeight * 0.9);
        int margin = (int) (rowHeight * 0.1);

        g.fillRect(x, yCounter, width, rowHeight * maxRows);
        FontMetrics metrics;
        g.setFont(font);
        float initialSize = g.getFont().getSize();
        StringFitter userMetadata = new StringFitterBuilder(initialSize, (int) (width * 0.55))
                .setBaseFont(g.getFont())
                .setMinSize(14).build();
        metrics = g.getFontMetrics(g.getFont());
        List<ReturnNowPlaying> nowPlayingArtistList = wrapperReturnNowPlaying.getReturnNowPlayings();
        yCounter += metrics.getAscent() + metrics.getDescent();
        for (int i = 0; i < nowPlayingArtistList.size() && i < 10; i++) {
            g.setColor(colorB1);

            g.fillRect(x, yCounter - metrics.getAscent() - metrics.getDescent(), width, rowContent);

            g.setColor(GraphicUtils.getBetter(colorB1));

            String name = nowPlayingArtistList.get(i).getDiscordName();
            Font tempFont = g.getFont();

            int startName = x;
            if (doNumber) {
                String strNumber = "#" + (phase + i + 1) + " ";
                g.drawString(strNumber, x, yCounter + (margin - metrics.getAscent() / 2));
                startName += g.getFontMetrics().stringWidth(strNumber);
            }


            StringFitter.FontMetadata fontMetadata = userMetadata.getFontMetadata(g, name);

            g.drawString(fontMetadata.atrribute().getIterator(), startName, yCounter + (margin - metrics.getAscent() / 2));

            g.setFont(tempFont.deriveFont(initialSize));
            String plays = String.valueOf(nowPlayingArtistList.get(i).getPlayNumber());
            int stringWidth = metrics.stringWidth(plays);
            int playPos = x + width - (rowHeight + stringWidth);
            int playEnd = playPos + stringWidth;
            g.drawString(plays, x + width - (rowHeight + metrics.stringWidth(plays)), yCounter + (margin - metrics
                                                                                                                   .getAscent() / 2));
            g.drawImage(lastFmLogo, playEnd + 9, (int) (yCounter - metrics.getAscent() * 0.85), null);
            yCounter += rowHeight;

        }
        g.setFont(ogFont);
        g.setColor(ogColor);
    }

    public static boolean isWhiter(Color col, Color col2) {
        double accum1 = 0.2126 * col.getRed() + 0.7152 * col.getGreen() + 0.0722 * col.getBlue();
        double accum2 = 0.2126 * col2.getRed() + 0.7152 * col2.getGreen() + 0.0722 * col2.getBlue();
        return accum1 >= accum2;

    }

    public static Color getBetter(Color... color) {
        double accum = 0;
        for (Color col : color) {
            accum += 0.2126 * col.getRed() + 0.7152 * col.getGreen() + 0.0722 * col.getBlue();
        }
        return (accum / color.length) < 128 ? Color.WHITE : Color.BLACK;

    }


    static void drawStringNicely(Graphics2D g, String string, int x, int y, BufferedImage bufferedImage) {

        Color temp = g.getColor();
        int length = g.getFontMetrics().stringWidth(string);
        try {
            Color col1 = new Color(bufferedImage.getRGB(
                    Math.min(bufferedImage.getWidth() - 1, Math.max(0, x))
                    , y));
            Color col2 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length / 2)), y));
            Color col3 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length)), y));
            g.setColor(getBetter(col1, col2, col3));
        } catch (ArrayIndexOutOfBoundsException debugger) {
            Chuu.getLogger().warn(x + " " + y + " " + " " + length + " " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
            Chuu.getLogger().warn(debugger.getMessage(), debugger);
        }
        g.drawString(string, x, y);
        g.setColor(temp);

    }

    static void drawStringNicely(Graphics2D g, StringFitter.FontMetadata fontMetadata, int x, int y, BufferedImage bufferedImage) {

        Color temp = g.getColor();
        int length = (int) fontMetadata.bounds().getWidth();
        try {
            Color col1 = new Color(bufferedImage.getRGB(
                    Math.min(bufferedImage.getWidth() - 1, Math.max(0, x))
                    , y));
            Color col2 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length / 2)), y));
            Color col3 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length)), y));
            g.setColor(getBetter(col1, col2, col3));
        } catch (ArrayIndexOutOfBoundsException debugger) {
            Chuu.getLogger().warn(x + " " + y + " " + " " + length + " " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
            Chuu.getLogger().warn(debugger.getMessage(), debugger);
        }
        g.drawString(fontMetadata.atrribute().getIterator(), x, y);
        g.setColor(temp);

    }

    static void drawStringChartly(Graphics2D g, String string, int x, int y) {
        Color temp = g.getColor();
        g.setColor(Color.BLACK);
        g.drawString(string, x, y);
        g.setColor(Color.WHITE);
        g.drawString(string, x + 1, y);
        g.setColor(temp);
    }

    static void drawStringChartly(Graphics2D g, StringFitter.FontMetadata string, int x, int y) {
        Color temp = g.getColor();
        g.setColor(Color.BLACK);
        g.drawString(string.atrribute().getIterator(), x, y);
        g.setColor(Color.WHITE);
        g.drawString(string.atrribute().getIterator(), x + 1, y);
        g.setColor(temp);
    }


    public static void initRandomImageBlurredBackground(final Graphics2D g, final int SIZE_X, final int SIZE_Y) {
        BufferedImage bim;

        Properties properties = new Properties();
        File[] files = walpepes.listFiles();
        if (files == null) {
            return;
        }
        File file;
        do {
            file = files[GraphicUtils.ran.nextInt(files.length)];
        } while (file.isDirectory());
        try {
            BufferedImage temp = ImageIO.read(file);
            if (temp != null) {
                bim = cropImage(temp, SIZE_X, SIZE_Y);
                temp.flush();
                g.drawImage(bim, new GaussianFilter(75), 0, 0);
                bim.flush();
            }
        } catch (IOException e) {
            Chuu.getLogger().info("Error reading file {} ", file.getName());
        }

    }

    private static BufferedImage cropImage(final BufferedImage src, final int SOURCE_X, final int SOURCE_Y) {

        int height = src.getTileHeight();
        int width = src.getTileWidth();
        int limity = height - SOURCE_Y;
        int limitx = width - SOURCE_X;
        if (limity <= 0 || limitx <= 0) {
            return Scalr.resize(src, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, SOURCE_X, SOURCE_Y, Scalr.OP_ANTIALIAS);
        }

        int y = ran.nextInt(limity);
        int x = ran.nextInt(limitx);
        return (src.getSubimage(x, y, SOURCE_X, SOURCE_Y));


    }

    /**
     * @param stringed    String to fit
     * @param g           Graphics2D instance
     * @param maxWidth    Max Width allowed
     * @param minFontSize Min font Size allowd
     * @return the width of the string on the current font
     */
    public static Rectangle2D fitAndGetBounds(String stringed, Graphics2D g, int maxWidth, float minFontSize) {
        Font ogFont = g.getFont();
        float sizeFont = ogFont.getSize();
        while (g.getFontMetrics(g.getFont()).stringWidth(stringed) > maxWidth && sizeFont > minFontSize) {
            g.setFont(g.getFont().deriveFont(sizeFont -= 2));
        }
        return g.getFontMetrics().getStringBounds(stringed, g);
    }

    public static BufferedImage getImage(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String path = url.replaceAll("[\\\\/:;?<>\"|*]", "_");
        path = path.substring(0, Math.min(path.length(), 150));
        File file = new File(CacheDirectory, path);
        if (file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (IOException e) {
                return downloadImage(url, file);
            }
        } else {
            return downloadImage(url, file);
        }
    }

    private static BufferedImage downloadImage(String url, File file) {
        try {
            URL uri = new URL(url);
            BufferedImage read = ImageIO.read(uri);
            if (read != null) {
                ImageIO.write(read, "png", file);
            }
            return read;
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

}

