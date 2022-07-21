package core.imagerenderer;

import core.Chuu;
import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.ReturnNowPlaying;
import dao.entities.WrapperReturnNowPlaying;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class ExetricWKMaker {
    public static final int IMG_SIZE = 464;
    private static final int X_MAX = 800;
    private static final int Y_MAX = 464;
    private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 26);
    private static final StringFitter artistFitter = new StringFitterBuilder(34, IMG_SIZE - 30).setStep(1).setBaseFont(NORMAL_FONT).setMinSize(14f).build();
    private static final Font DESC_FONT = new Font("Noto Sans CJK JP Light", Font.BOLD, 26);

    private static final StringFitter serverFitter = new StringFitterBuilder(26, X_MAX).setStep(1).setBaseFont(DESC_FONT.deriveFont(Font.BOLD)).setMinSize(12f).build();

    private static final String FIRST_LINE = "Who knows";

    private ExetricWKMaker() {

    }

    public static BufferedImage generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, String discordName, String logoUrl, BufferedImage logo) {
        return generateWhoKnows(wrapperReturnNowPlaying, discordName, logoUrl, logo, GraphicUtils.getImage(wrapperReturnNowPlaying.getUrl()));
    }

    public static BufferedImage generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, String populationName, String logoUrl, BufferedImage logo, BufferedImage leftSide) {

        BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_ARGB);

        String artist;
        artist = wrapperReturnNowPlaying.getArtist().toUpperCase();
        FontMetrics metrics;

        int yCounter = 0;
        Graphics2D g = canvas.createGraphics();
        GraphicUtils.setQuality(g);


        boolean doGradient = true;
        BufferedImage backgroundImage = leftSide;
        if (backgroundImage == null) {
            backgroundImage = GraphicUtils.noArtistImage;
            doGradient = false;
        }


        int cropStartX = 0;
        int cropStartY = 0;
        int h = backgroundImage.getHeight();
        int w = backgroundImage.getWidth();
        BufferedImage cover;
        if (w != h) {
            int constraint = Math.min(h, w);
            if (h > IMG_SIZE || w > IMG_SIZE) {
                if (h == constraint) {
                    cropStartX = (w - h) / 2;
                }
                if (w == constraint) {
                    cropStartY = (h - w) / 2;
                }
                BufferedImage cropped = Scalr.crop(backgroundImage, cropStartX, cropStartY, constraint, constraint, Scalr.OP_ANTIALIAS);
                cover = Scalr.resize(cropped, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, IMG_SIZE, Scalr.OP_ANTIALIAS);
                cropped.flush();

            } else {
                cover = Scalr.resize(backgroundImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, IMG_SIZE, Scalr.OP_ANTIALIAS);
            }
        } else {
            cover = Scalr.resize(backgroundImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, IMG_SIZE, Scalr.OP_ANTIALIAS);
        }
        backgroundImage.flush();
        Composite composite = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(0, 0, X_MAX, Y_MAX, 20, 20));
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(cover, 0, 0, null);
        g.setComposite(composite);

        Color[] sample = GraphicUtils.sampleBackground(cover);
        Color whiteOrBlack = GraphicUtils.getBetterSO(sample);
        Color canvasColor = GraphicUtils.mergeColor(sample);

        Color bordercolor = GraphicUtils.setAlpha(canvasColor.darker().darker().darker(), 0.8f); // DARker
        Color backgroundColor = GraphicUtils.setAlpha(canvasColor.brighter(), 0.5f); // Lighter

        if (whiteOrBlack != Color.BLACK) {
            // Border not visible
            if (GraphicUtils.contrast(bordercolor, backgroundColor) <= 2.) {
                bordercolor = GraphicUtils.setAlpha(canvasColor.brighter().brighter(), 0.8f);
            }
            Color temp = backgroundColor;
            backgroundColor = bordercolor;
            bordercolor = temp;
        }
        if (GraphicUtils.contrast(whiteOrBlack, backgroundColor) <= 2) {
            Chuu.getLogger().info("Bad contrast for {} | Image {} | Fore {} | Background ", artist, whiteOrBlack, backgroundColor);
            if (whiteOrBlack == Color.WHITE) {
                backgroundColor = GraphicUtils.setAlpha(backgroundColor.darker(), backgroundColor.getAlpha() / 255.f);
            } else {
                backgroundColor = GraphicUtils.setAlpha(backgroundColor.brighter(), backgroundColor.getAlpha() / 255.f);
            }
        }


        cover.flush();
        if (doGradient) {
            Color gp = Color.decode("#121212");
            GradientPaint gp1 = new GradientPaint(0, 200 + 10, GraphicUtils.setAlpha(gp, 0.0f), 0, 0, GraphicUtils.setAlpha(gp, 0.7f), false);
            g.setPaint(gp1);
            g.fillRect(0, 0, IMG_SIZE, 210);
        }
        g.setColor(Color.WHITE);
        g.setFont(DESC_FONT);

        GraphicUtils.drawStringNicely(g, FIRST_LINE, 30, 40, canvas, 0.5f);
        StringFitter.FontMetadata fontMetadata = artistFitter.getFontMetadata(g, artist);
        metrics = g.getFontMetrics(fontMetadata.maxFont());
        yCounter = 50;
        yCounter += metrics.getAscent() - metrics.getDescent() - metrics.getLeading();
        GraphicUtils.drawStringNicely(g, fontMetadata, 30, yCounter, canvas);


        StringFitter.FontMetadata serverMetadata = serverFitter.getFontMetadata(g, populationName);
        GraphicUtils.drawStringNicely(g, serverMetadata, 86, Y_MAX - 39, canvas);


        BufferedImage logoCover;
        if (logoUrl != null) {
            logoCover = GraphicUtils.getImage(logoUrl);
        } else {
            if (logo != null) {
                logoCover = logo;
            } else {
                logoCover = GraphicUtils.noArtistImage;
            }
        }

        if (logoCover != null) {
            BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            GraphicUtils.setQuality(g2);
            g2.setComposite(AlphaComposite.Src);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, 40, 40, 40, 40));
            g2.setComposite(AlphaComposite.SrcIn);
            g2.drawImage(logoCover, 0, 0, 40, 40, null);
            g2.dispose();

            g.setColor(GraphicUtils.getBetter(new Color(canvas.getRGB(30, Y_MAX - 70)), new Color(canvas.getRGB(70, Y_MAX - 70))));
            g.drawImage(image, 30, Y_MAX - 70, null);
            Stroke stroke = g.getStroke();
            g.setStroke(new BasicStroke(2));
            g.draw(new RoundRectangle2D.Float(30, Y_MAX - 70, 40, 40, 40, 40));
            g.setStroke(stroke);
        }
        if (logo != null) g.drawImage(logo, IMG_SIZE - logo.getWidth() - 15, Y_MAX - 15 - logo.getHeight(), null);
        g.setComposite(AlphaComposite.SrcOver);

        doChart(g, IMG_SIZE + 3, 0, X_MAX - IMG_SIZE - 3, Y_MAX, 10, wrapperReturnNowPlaying, backgroundColor, bordercolor, null, true, DESC_FONT, 0);
        g.setColor(bordercolor);
        g.setBackground(bordercolor);
        g.setPaint(bordercolor);
        // ????? One fill makes the colour clear
        g.fillRect(IMG_SIZE, 0, 3, Y_MAX);
        g.fillRect(IMG_SIZE, 0, 3, Y_MAX);
        g.dispose();

        return canvas;
    }

    public static void doChart(Graphics2D g, int x, int y, int width, int height, int maxRows, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color backgroundColor, Color borderColour, BufferedImage lastFmLogo, boolean doNumber, Font font, int phase) {
        int POS_M = 28;
        g.setColor(backgroundColor);
        g.fillRect(x, y, width, height);

        long itemSize = Math.round((double) height / maxRows);
        Font numberFont = NORMAL_FONT.deriveFont(22f).deriveFont(Font.BOLD);
        g.setFont(font);
        float initialSize = 24f;

        List<ReturnNowPlaying> nowPlayingArtistList = wrapperReturnNowPlaying.getReturnNowPlayings();
        StringFitter userMetadata = new StringFitterBuilder(initialSize, width).setBaseFont(g.getFont()).setMinSize(14).build();

        for (int i = 0; i < nowPlayingArtistList.size() || i < maxRows; i++) {
            g.setColor(GraphicUtils.getBetterSO(backgroundColor));
            if (i < nowPlayingArtistList.size()) {


                ReturnNowPlaying rnp = nowPlayingArtistList.get(i);
                String name = rnp.getDiscordName();
                long index = rnp.getIndex();

                Font prev = g.getFont();

                g.setFont(numberFont);
                FontMetrics metrics = g.getFontMetrics();
                String plays = String.valueOf(rnp.getPlayNumber());
                int stringWidth = metrics.stringWidth(plays);
                int userWidth = width - stringWidth - (15 + POS_M + 45);
                g.setFont(prev);
                g.setColor(GraphicUtils.setAlpha(g.getColor(), 1));

                StringFitter.FontMetadata fontMetadata = userMetadata.getFontMetadata(g, name, userWidth);
                LineMetrics lm = fontMetadata.maxFont().getLineMetrics(fontMetadata.atrribute().getIterator(), 0, name.length(), g.getFontRenderContext());
                float mHeights = lm.getAscent() - lm.getDescent() - lm.getLeading();
                int baseLine = (int) (y + (((itemSize - 3) / 2) + mHeights / 2));

                g.drawString(fontMetadata.atrribute().getIterator(), x + POS_M + 45, baseLine);

                g.setFont(numberFont);
                g.setColor(GraphicUtils.setAlpha(g.getColor(), 0.9f));
                if (doNumber) {
                    String strNumber = String.valueOf((phase + index + 1));
                    g.drawString(strNumber, x + POS_M, baseLine);
                }
                int playPos = x + width - (stringWidth + POS_M);
                g.drawString(plays, playPos, baseLine);
            }
            if (i < maxRows - 1) {
                g.setColor(borderColour);
                g.fillRect(x, (int) (y + itemSize - 3), width, 3);
                y += itemSize;
            }
        }
    }


}
