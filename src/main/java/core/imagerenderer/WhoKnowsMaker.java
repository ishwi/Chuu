package core.imagerenderer;

import core.imagerenderer.stealing.blur.GaussianFilter;
import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.WrapperReturnNowPlaying;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class WhoKnowsMaker {
    static final Font EMOJI_FONT = new Font("Symbola", Font.PLAIN, 32);
    private static final int X_MAX = 800;
    private static final int Y_MAX = 500;
    private static final int X_MARGIN = 80;
    private static final int Y_MARGIN = 25;
    private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 32);
    private static final StringFitter artistFitter = new StringFitterBuilder(32, X_MAX)
            .setStep(1)
            .setBaseFont(NORMAL_FONT)
            .setMinSize(14f).build();


    private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP", Font.PLAIN, 32);
    private static final Font DESC_FONT = new Font("Noto Sans CJK JP Light", Font.PLAIN, 32);

    private static final StringFitter serverFitter = new StringFitterBuilder(32, X_MAX)
            .setStep(1)
            .setBaseFont(DESC_FONT)
            .setMinSize(32f).build();

    private static final String FIRST_LINE = "Who knows";

    private WhoKnowsMaker() {

    }

    public static BufferedImage generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, String discordName, BufferedImage logo) {
        return generateWhoKnows(wrapperReturnNowPlaying, discordName, logo, GraphicUtils.getImage(wrapperReturnNowPlaying.getUrl()));
    }

    public static BufferedImage generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, String discordName, BufferedImage logo, BufferedImage leftSide) {

        BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
        String artist;
        artist = wrapperReturnNowPlaying.getArtist().toUpperCase();
        String urlString = wrapperReturnNowPlaying.getUrl();
        FontMetrics metrics;

        int width;
        int yCounter = 0;
        yCounter += Y_MARGIN;
        BufferedImage lastFmLogo = null;

        Graphics2D g = canvas.createGraphics();
        GraphicUtils.setQuality(g);
        BufferedImage backgroundImage = leftSide;
        if (backgroundImage == null) {
            backgroundImage = GraphicUtils.noArtistImage;
        }


        try (InputStream resourceAsStream = WhoKnowsMaker.class
                .getResourceAsStream("/images/logo2.png")) {
            if (resourceAsStream != null) {
                lastFmLogo = ImageIO.read(resourceAsStream);
            }
        } catch (IOException ignored) {
        }


        g.drawImage(backgroundImage, 0, 0, X_MAX, Y_MAX, 0, 0, backgroundImage.getWidth(), backgroundImage
                .getHeight(), null);
        new GaussianFilter(70).filter(canvas, canvas);

        Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
        Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));

        g.setFont(DESC_FONT);
        metrics = g.getFontMetrics(DESC_FONT);
        width = metrics.stringWidth(FIRST_LINE);
        yCounter += metrics.getAscent() - metrics.getDescent();

        GraphicUtils.drawStringNicely(g, FIRST_LINE, X_MAX / 2 - width / 2, yCounter, canvas);

        yCounter += 10;
        Font fontToUse;

        StringFitter.FontMetadata fontMetadata = artistFitter.getFontMetadata(g, artist);
        width = (int) fontMetadata.bounds().getWidth();
        metrics = g.getFontMetrics(fontMetadata.maxFont());
        yCounter += metrics.getAscent() - metrics.getDescent();
        GraphicUtils.drawStringNicely(g, fontMetadata, X_MAX / 2 - width / 2, yCounter, canvas);
        yCounter += metrics.getDescent();
        g.setFont(DESC_FONT);
        metrics = g.getFontMetrics(DESC_FONT);
        yCounter += metrics.getAscent() - metrics.getDescent();
        String thirdLine = "in " + discordName;

        StringFitter.FontMetadata serverMetadata = serverFitter.getFontMetadata(g, thirdLine);

        width = (int) serverMetadata.bounds().getWidth();
        GraphicUtils.drawStringNicely(g, serverMetadata, X_MAX / 2 - width / 2, yCounter, canvas);
        yCounter += 16;

        int rectWidth = X_MAX - X_MARGIN - (X_MARGIN + 320);

        backgroundImage = Scalr.resize(backgroundImage, Scalr.Method.QUALITY, 320, Scalr.OP_ANTIALIAS);
        int xImageStarter = X_MARGIN + (320 - backgroundImage.getWidth()) / 2;
        int yImageStarter = yCounter + (320 - backgroundImage.getHeight()) / 2;
        g.drawImage(backgroundImage, xImageStarter, yImageStarter, null);
        if (logo != null)
            g.drawImage(logo, X_MARGIN + 320 + rectWidth - logo.getWidth(), yCounter - 16 - logo
                    .getHeight(), null);

//        if (modes.contains(WKMode.RANK)) {
//            boolean draw = false;
//            int j = 0;
//            List<ReturnNowPlaying> returnNowPlayings = wrapperReturnNowPlaying.getReturnNowPlayings();
//            for (int returnNowPlayingsSize = returnNowPlayings.size(); j < returnNowPlayingsSize; j++) {
//                ReturnNowPlaying returnNowPlaying = returnNowPlayings.get(j);
//                if (returnNowPlaying.getDiscordId() == author && j >= 10) {
//                    draw = true;
//                    break;
//                }
//            }
//            if (draw) {
//                WrapperReturnNowPlaying authorWrapper = new WrapperReturnNowPlaying(List.of(wrapperReturnNowPlaying.getReturnNowPlayings().get(j)), 1, wrapperReturnNowPlaying.getUrl(), wrapperReturnNowPlaying.getArtist());
//                GraphicUtils
//                        .doChart(g, X_MARGIN + 320, yCounter + 334, rectWidth, 32, 10, authorWrapper, colorB1, colorB, lastFmLogo, true, DESC_FONT.deriveFont(18f), j + 1);
//            }
//        }

        GraphicUtils
                .doChart(g, X_MARGIN + 320, yCounter, rectWidth, 32, 10, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, DESC_FONT
                        .deriveFont(18f));
        return canvas;
    }


}
