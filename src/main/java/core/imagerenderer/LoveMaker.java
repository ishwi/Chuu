package core.imagerenderer;

import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.Affinity;
import dao.entities.DiscordUserDisplay;
import dao.entities.UserArtistComparison;
import org.apache.commons.lang3.tuple.Pair;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static core.imagerenderer.GraphicUtils.chooseFont;

public class LoveMaker {

    private final static int X_SIZE = 1400;
    private final static int Y_SIZE = 350;
    private final static int X_MARGIN = 100;
    private final static int Y_MARGIN = 25;
    private final static int IMAGE_MARGIN = 5;
    private static final int IMAGE_SIZE = 150;
    private static final float TITLE_SIZE = 48;
    private static final float SUBTITLE_IMAGE_SIZE = 28;
    private static final float DESC_SIZE = 26;
    private static final int BAR_SIZE = X_SIZE - (X_MARGIN + IMAGE_SIZE + IMAGE_MARGIN) * 2;
    private static final Font NORMAL_FONT = new Font("Noto Sans", Font.BOLD, (int) DESC_SIZE);
    private static final Font JAPANESE_FONT = new Font("Yu Gothic", Font.BOLD, (int) DESC_SIZE);
    private static final Font KOREAN_FONT = new Font("Malgun Gothic", Font.BOLD, (int) DESC_SIZE);
    private static final Font EMOJI_FONT = new Font("Symbola", Font.PLAIN, (int) DESC_SIZE);

    private LoveMaker() {
    }

    public static BufferedImage calculateLove(Affinity affinity, DiscordUserDisplay firstUser, String firstImage, String secondImage, DiscordUserDisplay secondUser) {

        BufferedImage canvas = new BufferedImage(X_SIZE, Y_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = canvas.createGraphics();
        GraphicUtils.setQuality(g);
        GraphicUtils.initRandomImageBlurredBackground(g, X_SIZE, Y_SIZE);

        BufferedImage first = getImageFromUrl(firstImage);
        int xImageStarter = X_MARGIN + (IMAGE_SIZE - first.getWidth()) / 2;
        int yImageStarter = Y_MARGIN + (IMAGE_SIZE - first.getHeight()) / 2;
        g.drawImage(first, xImageStarter, yImageStarter, null);

        BufferedImage second = getImageFromUrl(secondImage);
        xImageStarter = X_SIZE - X_MARGIN - (IMAGE_SIZE + second.getWidth()) / 2;
        yImageStarter = Y_MARGIN + (IMAGE_SIZE - second.getHeight()) / 2;
        g.drawImage(second, xImageStarter, yImageStarter, null);
        g.setColor(GraphicUtils.makeMoreTransparent(Color.GRAY, 0.7f));

        g.fillRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), BAR_SIZE, (IMAGE_SIZE / 2));
        if (affinity.getAffinity() > 0.75f) {
            g.setColor(Color.RED);
        } else if (affinity.getAffinity() > 0.5f) {
            g.setColor(Color.ORANGE);
        } else if (affinity.getAffinity() > 0.25f) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.GREEN);
        }

        g.setColor(GraphicUtils.makeMoreTransparent(g.getColor(), 0.7f));
        g.fillRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), (int) (Math.min(affinity.getAffinity(), 1f) * (BAR_SIZE)), (IMAGE_SIZE / 2));
        g.setColor(Color.BLACK);
        g.drawRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), BAR_SIZE, (IMAGE_SIZE / 2) - 1);
        g.drawRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), (int) (Math.min(affinity.getAffinity(), 1f) * (BAR_SIZE)), (IMAGE_SIZE / 2) - 1);
        String format;
        if (affinity.getAffinity() > 1) {
            format = "COMPATIBILITY: 100%+";
        } else {
            format = String.format("COMPATIBILITY: %.0f%%", affinity.getAffinity() * 100);
        }
        g.setFont(NORMAL_FONT.deriveFont(Font.BOLD, TITLE_SIZE));
        Rectangle2D titleBound = g.getFontMetrics().getStringBounds(format, g);
        GraphicUtils.drawStringNicely(g, format, (X_SIZE / 2 - ((int) titleBound.getWidth() / 2)), Y_MARGIN + (IMAGE_SIZE / 2) - 25, canvas);


        StringFitter userBuilder = new StringFitterBuilder(SUBTITLE_IMAGE_SIZE, IMAGE_SIZE)
                .setStyle(Font.BOLD)
                .setMinSize(18).build();
        StringFitter.FontMetadata u1 = userBuilder
                .getFontMetadata(g, firstUser.username());
        StringFitter.FontMetadata u2 = userBuilder
                .getFontMetadata(g, secondUser.username());


        int baseline = (int) Math.max(u1.bounds().getHeight(), u2.bounds().getHeight());
        GraphicUtils.drawStringNicely(g, u1, X_MARGIN + (IMAGE_SIZE - (int) u1.bounds().getWidth()) / 2, Y_MARGIN + IMAGE_SIZE + baseline, canvas);
        GraphicUtils.drawStringNicely(g, u2, X_SIZE - X_MARGIN - (IMAGE_SIZE + (int) u2.bounds().getWidth()) / 2, Y_MARGIN + IMAGE_SIZE + baseline, canvas);


        int i = drawRecommendation(affinity.getReceivingRec(), firstUser, canvas, g, 0);
        int i2 = drawRecommendation(affinity.getOgRec(), secondUser, canvas, g, (int) (i * 1.5));
        g.setFont(NORMAL_FONT.deriveFont(DESC_SIZE));
        int lastLineBaseline = (int) (Y_MARGIN + IMAGE_SIZE + Y_MARGIN * 2 + i * 1.5 + i2 * 1.5);

        if (affinity.getMatchingList().isEmpty()) {
            String noMatching = String.format("Both of you don't share any common artists with more than %s plays :(", affinity.getThreshold());
            int i1 = g.getFontMetrics().stringWidth(noMatching);
            int startingPoint = (X_SIZE - i1) / 2;
            GraphicUtils.drawStringNicely(g, noMatching, startingPoint, lastLineBaseline, canvas);
        } else {
            Pair<Font[], Integer> lastLine = getLastLine(affinity.getMatchingList(), g);
            Integer right = lastLine.getRight();
            int startingPoint = Math.max(0, (X_SIZE - right) / 2);
            GraphicUtils.drawStringNicely(g, "You both love: ", startingPoint, lastLineBaseline, canvas);
            int i1 = g.getFontMetrics().stringWidth("You both love: ");
            drawMultiString(lastLine.getKey(), startingPoint + i1, lastLineBaseline, affinity.getMatchingList(), g, canvas);
        }
        g.dispose();
        return canvas;
    }

    private static BufferedImage getImageFromUrl(String firstImage) {
        BufferedImage first = GraphicUtils.getImageFromUrl(firstImage, GraphicUtils.noArtistImage);

        if (first.getHeight() > IMAGE_SIZE || first.getWidth() > IMAGE_SIZE)
            first = Scalr.resize(first, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, IMAGE_SIZE, Scalr.OP_ANTIALIAS);
        assert first.getWidth() <= IMAGE_SIZE && first.getHeight() <= IMAGE_SIZE;
        return first;
    }

    private static void drawMultiString(Font[] fonts, int startPoint, int baseline, List<UserArtistComparison> userArtistComparisons, Graphics2D g, BufferedImage canvas) {
        assert userArtistComparisons.size() == fonts.length;
        for (int i = 0; i < fonts.length; i++) {
            Font font = fonts[i];
            String str = userArtistComparisons.get(i).getArtistID();
            if (i == fonts.length - 2) {
                str += " and ";
            } else if (i != fonts.length - 1) {
                str += ", ";
            }
            g.setFont(font);
            GraphicUtils.drawStringNicely(g, str, startPoint, baseline, canvas);
            startPoint += g.getFontMetrics().stringWidth(str);

        }
    }

    private static Pair<Font[], Integer> getLastLine(List<UserArtistComparison> comparisonList, Graphics2D g) {
        Font[] fonts = new Font[comparisonList.size()];
        float FONT_SIZE = DESC_SIZE;
        int wholeSize = g.getFontMetrics(NORMAL_FONT.deriveFont(FONT_SIZE)).stringWidth("You both love: and ");
        for (int i = 0, comparisonListSize = comparisonList.size(); i < comparisonListSize; i++) {
            UserArtistComparison x = comparisonList.get(i);
            Font font = chooseFont(x.getArtistID());
            font = font.deriveFont(Font.BOLD, FONT_SIZE);
            wholeSize += g.getFontMetrics(font).stringWidth(x.getArtistID() + ", ");
            fonts[i] = font;
        }
        return Pair.of(fonts, wholeSize);


    }

    public static int drawRecommendation(String artist, DiscordUserDisplay secondUser, BufferedImage canvas, Graphics2D g, int yAccum) {
        if (artist != null) {
            String headerLine = String.format("Recommendation from %s: ", secondUser.username());
            StringFitter recFitter = new StringFitterBuilder(DESC_SIZE, (int) (X_SIZE * 0.8))
                    .setStyle(Font.BOLD)
                    .setMinSize(18).build();

            StringFitter.FontMetadata fontMetadata = recFitter.getFontMetadata(g, headerLine + artist);
            Rectangle2D secondPartBounds = fontMetadata.bounds();

            int secondPartStart = (int) ((X_SIZE - secondPartBounds.getWidth()) / 2);
            int secondPartBaseline = Y_MARGIN + IMAGE_SIZE + Y_MARGIN * 2;
            GraphicUtils.drawStringNicely(g, fontMetadata, secondPartStart, secondPartBaseline + yAccum, canvas);
            return (int) secondPartBounds.getHeight();
        } else {
            String title = secondUser.username() + " couldn't give a reccomendation :( ";
            g.setFont(NORMAL_FONT.deriveFont(DESC_SIZE));
            StringFitter recFitter = new StringFitterBuilder(DESC_SIZE, (int) (X_SIZE * 0.8))
                    .setMinSize(18).build();
            StringFitter.FontMetadata fontMetadata = recFitter.getFontMetadata(g, title);
            Rectangle2D secondPartBounds = fontMetadata.bounds().getBounds();
            int secondPartStart = (int) ((X_SIZE - secondPartBounds.getWidth()) / 2);
            int secondPartBaseline = Y_MARGIN + IMAGE_SIZE + Y_MARGIN * 2;
            GraphicUtils.drawStringNicely(g, fontMetadata, secondPartStart, secondPartBaseline + yAccum, canvas);
            return (int) secondPartBounds.getHeight();
        }
    }

}
