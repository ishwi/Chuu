package core.imagerenderer;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.imagerenderer.stealing.blur.GaussianFilter;
import core.imagerenderer.stealing.colorpicker.ColorThiefCustom;
import core.imagerenderer.util.CIELab;
import core.imagerenderer.util.D;
import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import core.services.ChuuRunnable;
import core.util.VirtualParallel;
import dao.entities.ReturnNowPlaying;
import dao.entities.WrapperReturnNowPlaying;
import dao.exceptions.ChuuServiceException;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.tuple.Pair;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Stream;

public class GraphicUtils {
    public static final BufferedImage noArtistImage;
    public static final Font NORMAL_FONT = new Font("Noto Sans", Font.PLAIN, 14);
    static final Random ran = new Random();
    static final Path CACHE_DIRECTORY;
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
    private static final Path wallpaperDir;
    public static List<Path> wallpapers;

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
            wallpaperDir = Paths.get(path);
            try (Stream<Path> list = Files.list(wallpaperDir)) {
                wallpapers = list.filter(f -> !Files.isDirectory(f)).toList();
            }
            CACHE_DIRECTORY = Paths.get(cache_folder);
            if (!Files.isDirectory(CACHE_DIRECTORY)) throw new AssertionError();
            noArtistImage = ImageIO.read(in2);
        } catch (IOException e) {
            throw new IllegalStateException("/images/noArtistImage.png should exists under resources!!");
        }
    }

    private GraphicUtils() {
    }


    public static Color slightlyDarker(Color color, double factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0),
                color.getAlpha());
    }

    private static int clamp(int input) {
        return Math.max(0, Math.min(input, 255));
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
            i = clamp(i);
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


    public static void drawImageInCorner(String urlImage, Graphics2D g) {
        BufferedImage read = GraphicUtils.getImage(urlImage);
        if (read != null) {
            BufferedImage image = GraphicUtils.resizeOrCrop(read, 100);
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

    static Color getFontColorBackground(BufferedImage canvas) {
        int a = canvas.getRGB(5, 5);
        return new Color(a);
    }

    static Color sampleBackground(BufferedImage canvas) {
        Pair<List<Color>, Color> palette = ColorThiefCustom.getPalette(canvas, 5, 10, false);
        List<Color> left = palette.getLeft();
        Color dominant = palette.getRight();
        List<Color> colors = new ArrayList<>();
        if (left != null) {
            colors.addAll(left);
        }
        if (dominant != null) {
            colors.add(dominant);
        }
        return mergeColor(colors.toArray(Color[]::new));


    }

    static Color mergeColor(Color... colors) {
        float r = 0;
        float g = 0;
        float b = 0;
        for (Color color : colors) {
            float[] chan = color.getColorComponents(null);
            r += chan[0];
            g += chan[1];
            b += chan[2];
        }
        return new Color(r / colors.length, g / colors.length, b / colors.length);
    }

    public static Color setAlpha(Color fontColor, float percentage) {
        float[] rgb2 = new float[3];
        fontColor.getRGBColorComponents(rgb2);
        return new Color(rgb2[0], rgb2[1], rgb2[2], percentage);

    }

    static Color getReadableColorBackgroundForFont(Color fontColor) {
        float[] rgb2 = new float[3];
        fontColor.getRGBColorComponents(rgb2);
        Color colorB1 = new Color(rgb2[0], rgb2[1], rgb2[2], 0.8f);
        return colorB1.darker().darker().darker();
    }

    static Color getSurfaceColor(Color fontColor) {
        float[] rgb2 = new float[3];
        fontColor.getRGBColorComponents(rgb2);
        return new Color(rgb2[0], rgb2[1], rgb2[2], 0.5f).brighter();
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

            ReturnNowPlaying rnp = nowPlayingArtistList.get(i);
            String name = rnp.getDiscordName();
            long index = rnp.getIndex();
            Font tempFont = g.getFont();

            int startName = x;
            if (doNumber) {
                String strNumber = "#" + (phase + index + 1) + " ";
                g.drawString(strNumber, x, yCounter + (margin - metrics.getAscent() / 2));
                startName += g.getFontMetrics().stringWidth(strNumber);
            }


            StringFitter.FontMetadata fontMetadata = userMetadata.getFontMetadata(g, name);

            g.drawString(fontMetadata.atrribute().getIterator(), startName, yCounter + (margin - metrics.getAscent() / 2));

            g.setFont(tempFont.deriveFont(initialSize));
            String plays = String.valueOf(rnp.getPlayNumber());
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
            float[] rgbComponents = col.getRGBComponents(null);
            double r = rgbComponents[0];
            double g = rgbComponents[1];
            double b = rgbComponents[2];
            accum += perceivedLightness(luminance(r, g, b));
        }
        return (accum / color.length) <= 50 ? Color.WHITE : Color.BLACK;

    }

    private static double sRGBtoLin(double chann) {
        if (chann <= 0.04045) {
            return chann / 12.92;
        } else {
            return Math.pow((chann + 0.055) / 1.055, 2.4);
        }
    }

    private static double luminance(double r, double g, double b) {
        return 0.2126 * sRGBtoLin(r) + 0.7152 * sRGBtoLin(g) + 0.0722 * sRGBtoLin(b);
    }

    public static double perceivedLightness(double y) {
        if (y <= 216 / 24389.) {
            return y * (24389. / 27);
        } else {
            return Math.pow(y, 1 / 3.) * 116 - 16;
        }
    }

    public static double contrast(Color fore, Color back) {
        float[] rgbComponents = fore.getRGBComponents(null);
        double r = rgbComponents[0];
        double g = rgbComponents[1];
        double b = rgbComponents[2];


        double l0 = perceivedLightness(luminance(r, g, b));

        rgbComponents = back.getRGBComponents(null);
        r = rgbComponents[0];
        g = rgbComponents[1];
        b = rgbComponents[2];
        double l1 = perceivedLightness(luminance(r, g, b));
        return (l0 + 0.05) / (l1 + 0.05);
    }


    static void drawStringNicely(Graphics2D g, String string, int x, int y, BufferedImage bufferedImage) {
        drawStringNicely(g, string, x, y, bufferedImage, null);
    }

    static void drawStringNicely(Graphics2D g, String string, int x, int y, BufferedImage bufferedImage, Float transparency) {

        Color temp = g.getColor();
        int length = g.getFontMetrics().stringWidth(string);
        try {
            Color col1 = new Color(bufferedImage.getRGB(
                    Math.min(bufferedImage.getWidth() - 1, Math.max(0, x))
                    , y));
            Color col2 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length / 2)), y));
            Color col3 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length)), y));
            Color better = getBetter(col1, col2, col3);
            if (transparency != null) {
                better = GraphicUtils.setAlpha(better, transparency);
            }
            g.setColor(better);
        } catch (ArrayIndexOutOfBoundsException debugger) {
            Chuu.getLogger().warn(x + " " + y + " " + " " + length + " " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
            Chuu.getLogger().warn(debugger.getMessage(), debugger);
        }
        g.drawString(string, x, y);
        g.setColor(temp);

    }

    static void drawStringNicely(Graphics2D g, StringFitter.FontMetadata fontMetadata, int x, int y, BufferedImage bufferedImage) {

        Color temp = g.getColor();
        setColorNicely(g, fontMetadata, x, y, bufferedImage);
        g.drawString(fontMetadata.atrribute().getIterator(), x, y);
        g.setColor(temp);

    }

    static void drawStringNicelyLayout(Graphics2D g, StringFitter.FontMetadata fontMetadata, int x, int y, BufferedImage bufferedImage, int width) {

        Color temp = g.getColor();
        setColorNicely(g, fontMetadata, x, y, bufferedImage);
        AttributedString atrribute = fontMetadata.atrribute();
        int endIndex = atrribute.getIterator().getEndIndex();
        LineBreakMeasurer measurer = new LineBreakMeasurer(atrribute.getIterator(), g.getFontRenderContext());
        while (measurer.getPosition() < endIndex) {
            TextLayout textLayout = measurer.nextLayout(width);
            textLayout.draw(g, x, y);
            y += textLayout.getAscent() - textLayout.getDescent() - textLayout.getLeading() + 10;
        }
        g.setColor(temp);

    }

    private static void setColorNicely(Graphics2D g, StringFitter.FontMetadata fontMetadata, int x, int y, BufferedImage bufferedImage) {
        int length = (int) fontMetadata.bounds().getWidth();
        try {
            Color col1 = new Color(bufferedImage.getRGB(
                    Math.min(bufferedImage.getWidth() - 1, Math.max(0, x))
                    , y));
            Color col2 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length / 2)), y));
            Color col3 = new Color(bufferedImage.getRGB(Math.max(0, Math.min(bufferedImage.getWidth() - 5, x + length)), y));
            g.setColor(getBetter(col1, col2, col3));
        } catch (ArrayIndexOutOfBoundsException e) {
            Chuu.getLogger().warn("%d %d %d %d %d".formatted(x, y, length, bufferedImage.getWidth(), bufferedImage.getHeight()));
            Chuu.getLogger().warn(e.getMessage(), e);
        }
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

        List<Path> files = wallpapers;
        if (ran.nextFloat() > 0.9995) {
            try (Stream<Path> list = Files.list(wallpaperDir)) {
                wallpapers = list.filter(f -> !Files.isDirectory(f)).toList();
            } catch (IOException e) {
                // Do nothing
            }
        }
        if (files == null) {
            return;
        }

        try {
            int i = GraphicUtils.ran.nextInt(files.size());
            Path file = files.get(i);
            BufferedImage temp = ImageIO.read(new BufferedInputStream(Files.newInputStream(file)));
            if (temp != null) {
                bim = cropImage(temp, SIZE_X, SIZE_Y);
                temp.flush();
                g.drawImage(bim, new GaussianFilter(75), 0, 0);
                bim.flush();
            }
        } catch (Exception e) {
            Chuu.getLogger().warn(e.getMessage(), e);
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

    public static BufferedImage getImage(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String path = url.replaceAll("[\\\\/:;?<>\"|*]", "_");
        path = path.substring(0, Math.min(path.length(), 150));
        Path file = CACHE_DIRECTORY.resolve(path);
        if (Files.exists(file)) {
            VirtualParallel.handleInterrupt();
            try (var is = new BufferedInputStream(Files.newInputStream(file))) {
                VirtualParallel.handleInterrupt();
                return ImageIO.read(is);
            } catch (IOException e) {
                Chuu.getLogger().warn("Error reading image {}", file, e);
                return downloadImage(url, file);
            } catch (Exception e) {
                Chuu.getLogger().warn("Error reading image {}", file, e);
                if (Thread.interrupted()) {
                    throw new ChuuServiceException(e);
                }
                return downloadImage(url, file);
            }

        } else {
            return downloadImage(url, file);
        }
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static BufferedImage downloadImage(String url, Path file) {
        try (var is = new BufferedInputStream(createCon(url).getInputStream())) {

            VirtualParallel.handleInterrupt();
            BufferedImage read = ImageIO.read(is);
            if (read != null) {
                BufferedImage copied = deepCopy(read);
                CommandUtil.runLog((ChuuRunnable) () -> {
                    try (var output = new BufferedOutputStream(Files.newOutputStream(file))) {
                        ImageIO.write(copied, "png", output);
                    }
                });
            }
            return read;
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            Chuu.getLogger().warn("Error downloading image {}", url, ex);
            VirtualParallel.handleInterrupt();
            return null;
        }
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

    public static ChartQuality getQuality(int chartSize, Context e) {
        ChartQuality chartQuality = ChartQuality.PNG_BIG;
        if ((e.isFromGuild() && e.getGuild().getMaxFileSize() == Message.MAX_FILE_SIZE) || !e.isFromGuild()) {
            if (chartSize > 45 && chartSize < 200)
                chartQuality = ChartQuality.JPEG_BIG;
            else if (chartSize >= 200)
                chartQuality = ChartQuality.JPEG_SMALL;
        } else if (e.getGuild().getMaxFileSize() == (50 << 20)) {
            if (chartSize > (45 * (50 / 8.)) && chartSize < 10000) {
                chartQuality = ChartQuality.JPEG_BIG;
            } else if (chartSize >= 10000) {
                chartQuality = ChartQuality.JPEG_SMALL;
            }
        } else {
            if (chartSize > (45 * (100 / 8.)) && chartSize < 20000) {
                chartQuality = ChartQuality.JPEG_BIG;
            } else if (chartSize >= 20000) {
                chartQuality = ChartQuality.JPEG_SMALL;
            }
        }
        return chartQuality;
    }

    static BufferedImage resizeOrCrop(BufferedImage backgroundImage, int size) {
        int cropStartX = 0;
        int cropStartY = 0;
        int h = backgroundImage.getHeight();
        int w = backgroundImage.getWidth();
        BufferedImage cover;
        if (w != h) {
            int constraint = Math.min(h, w);
            if (h > size || w > size) {
                if (h == constraint) {
                    cropStartX = (w - h) / 2;
                }
                if (w == constraint) {
                    cropStartY = (h - w) / 2;
                }
                BufferedImage cropped = Scalr.crop(backgroundImage, cropStartX, cropStartY, constraint, constraint, Scalr.OP_ANTIALIAS);
                cover = Scalr.resize(cropped, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, size, Scalr.OP_ANTIALIAS);
                cropped.flush();

            } else {
                cover = Scalr.resize(backgroundImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, size, Scalr.OP_ANTIALIAS);
            }
        } else {
            cover = Scalr.resize(backgroundImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, size, Scalr.OP_ANTIALIAS);
        }
        VirtualParallel.handleInterrupt();
        return cover;
    }

    private static URLConnection createCon(String url) throws IOException {
        URLConnection urlConnection = new URL(url)
                .openConnection();
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        urlConnection.setRequestProperty("sec-ch-ua", "Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
        urlConnection.setRequestProperty("sec-ch-ua-mobile", "?0");
        urlConnection.setRequestProperty("sec-ch-ua-platform", "\" Windows\"");
        urlConnection.setRequestProperty("sec-fetch-dest", "document");
        urlConnection.setRequestProperty("sec-fetch-mode", "navigate");
        urlConnection.setRequestProperty("sec-fetch-site", "none");
        urlConnection.setRequestProperty("sec-fetch-user", "?1");
        return urlConnection;
    }
}

