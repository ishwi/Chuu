package core.imagerenderer;

import dao.entities.Affinity;
import dao.entities.DiscordUserDisplay;
import dao.entities.UserArtistComparison;
import org.apache.commons.lang3.tuple.Pair;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class LoveMaker {
    private static final Font JAPANESE_FONT = new Font("Yu Gothic", Font.BOLD, (int) DESC_SIZE);

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
    private static final Font KOREAN_FONT = new Font("Malgun Gothic", Font.BOLD, (int) DESC_SIZE);
    private static final Font EMOJI_FONT = new Font("Symbola", Font.PLAIN, (int) DESC_SIZE);

    private LoveMaker() {
    }

    public static BufferedImage calculateLove(Affinity affinity, DiscordUserDisplay firstUser, String firstImage, String secondImage, DiscordUserDisplay secondUser) {

        BufferedImage canvas = new BufferedImage(X_SIZE, Y_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = canvas.createGraphics();
        GraphicUtils.setQuality(g);
        GraphicUtils.initRandomImageBlurredBackground(g, X_SIZE, Y_SIZE);

        BufferedImage first;
        try {
            first = ImageIO.read(new URL(firstImage));

        } catch (IOException e) {
            first = GraphicUtils.noArtistImage;
        }
        if (first.getHeight() > IMAGE_SIZE || first.getWidth() > IMAGE_SIZE)
            first = Scalr.resize(first, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, IMAGE_SIZE, Scalr.OP_ANTIALIAS);
        assert first.getWidth() <= IMAGE_SIZE && first.getHeight() <= IMAGE_SIZE;
        int xImageStarter = X_MARGIN + (IMAGE_SIZE - first.getWidth()) / 2;
        int yImageStarter = Y_MARGIN + (IMAGE_SIZE - first.getHeight()) / 2;
        g.drawImage(first, xImageStarter, yImageStarter, null);

        BufferedImage second;

        try {
            second = ImageIO.read(new URL(secondImage));

        } catch (IOException e) {
            second = GraphicUtils.noArtistImage;
        }
        if (second.getHeight() > IMAGE_SIZE || second.getWidth() > IMAGE_SIZE)
            second = Scalr.resize(second, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, IMAGE_SIZE, Scalr.OP_ANTIALIAS);

        assert second.getWidth() <= IMAGE_SIZE && second.getHeight() <= IMAGE_SIZE;
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
        g.fillRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), (int) (affinity.getAffinity() * (BAR_SIZE)), (IMAGE_SIZE / 2));
        g.setColor(Color.BLACK);
        g.drawRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), BAR_SIZE, (IMAGE_SIZE / 2) - 1);
        g.drawRect(X_MARGIN + IMAGE_MARGIN + IMAGE_SIZE, Y_MARGIN + (IMAGE_SIZE / 2), (int) (affinity.getAffinity() * (BAR_SIZE)), (IMAGE_SIZE / 2) - 1);
        String format;
        if (affinity.getAffinity() > 1) {
            format = "COMPATIBILITY: 100%%+";
        } else {
            format = String.format("COMPATIBILITY: %.0f%%", affinity.getAffinity() * 100);
        }
        g.setFont(NORMAL_FONT.deriveFont(Font.BOLD, TITLE_SIZE));
        Rectangle2D titleBound = g.getFontMetrics().getStringBounds(format, g);
        GraphicUtils.drawStringNicely(g, format, (X_SIZE / 2 - ((int) titleBound.getWidth() / 2)), Y_MARGIN + (IMAGE_SIZE / 2) - 25, canvas);


        g.setFont(NORMAL_FONT.deriveFont(SUBTITLE_IMAGE_SIZE));
        Rectangle2D f = GraphicUtils.fitAndGetBounds(firstUser.getUsername(), g, IMAGE_SIZE, 18f);
        Rectangle2D n = GraphicUtils.fitAndGetBounds(secondUser.getUsername(), g, IMAGE_SIZE, 18f);
        int baseline = (int) Math.max(f.getHeight(), n.getHeight());
        GraphicUtils.drawStringNicely(g, firstUser.getUsername(), X_MARGIN + (IMAGE_SIZE - (int) f.getWidth()) / 2, Y_MARGIN + IMAGE_SIZE + baseline, canvas);
        GraphicUtils.drawStringNicely(g, secondUser.getUsername(), X_SIZE - X_MARGIN - (IMAGE_SIZE + (int) n.getWidth()) / 2, Y_MARGIN + IMAGE_SIZE + baseline, canvas);


        int i = drawRecommendation(affinity.getReceivingRec(), firstUser, canvas, g, 0);
        int i2 = drawRecommendation(affinity.getOgRec(), secondUser, canvas, g, (int) (i * 1.5));

        int lastLineBaseline = (int) (Y_MARGIN + IMAGE_SIZE + Y_MARGIN * 2 + i * 1.5 + i2 * 1.5);

        if (affinity.getMatchingList().isEmpty()) {
            String noMatching = String.format("Both of you don't share any common artists with more than %s plays :(", affinity.getThreshold());
            int i1 = g.getFontMetrics().stringWidth(noMatching);
            int startingPoint = (X_SIZE - i1) / 2;
            GraphicUtils.drawStringNicely(g, noMatching, startingPoint, lastLineBaseline, canvas);
        } else {
            Pair<Font[], Integer> lastLine = getLastLine(affinity.getMatchingList(), g);
            Integer right = lastLine.getRight();
            int startingPoint = (X_SIZE - right) / 2;
            GraphicUtils.drawStringNicely(g, "You both love: ", startingPoint, lastLineBaseline, canvas);
            int i1 = g.getFontMetrics().stringWidth("You both love: ");
            drawMultiString(lastLine.getKey(), startingPoint + i1, lastLineBaseline, affinity.getMatchingList(), g, canvas);
        }
        g.dispose();
        return canvas;
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
            font = font.deriveFont(FONT_SIZE);
            wholeSize += g.getFontMetrics(font).stringWidth(x.getArtistID() + ", ");
            fonts[i] = font;
        }
        return Pair.of(fonts, wholeSize);


    }

    public static int drawRecommendation(String artist, DiscordUserDisplay secondUser, BufferedImage canvas, Graphics2D g, int yAccum) {
        if (artist != null) {
            String secondLinePart = String.format("Recommendation from %s: ", secondUser.getUsername());
            g.setFont(NORMAL_FONT.deriveFont(DESC_SIZE));
            Rectangle2D secondPartBounds = g.getFontMetrics().getStringBounds(secondLinePart + artist, g);
            int secondPartStart = (int) ((X_SIZE - secondPartBounds.getWidth()) / 2);
            int secondPartBaseline = Y_MARGIN + IMAGE_SIZE + Y_MARGIN * 2;
            GraphicUtils.drawStringNicely(g, secondLinePart, secondPartStart, secondPartBaseline + yAccum, canvas);
            Font secondFont = chooseFont(artist);
            secondFont.deriveFont((float) g.getFont().getSize());
            secondPartBounds = g.getFontMetrics().getStringBounds(secondLinePart, g);
            GraphicUtils.drawStringNicely(g, artist, (int) (secondPartStart + secondPartBounds.getWidth()), secondPartBaseline + yAccum, canvas);
            return (int) secondPartBounds.getHeight();
        } else {
            String title = secondUser.getUsername() + " couldn't give a reccomendation :( ";
            g.setFont(NORMAL_FONT.deriveFont(DESC_SIZE));
            Rectangle2D secondPartBounds = g.getFontMetrics().getStringBounds(title, g);
            int secondPartStart = (int) ((X_SIZE - secondPartBounds.getWidth()) / 2);
            int secondPartBaseline = Y_MARGIN + IMAGE_SIZE + Y_MARGIN * 2;
            GraphicUtils.drawStringNicely(g, title, secondPartStart, secondPartBaseline + yAccum, canvas);
            return (int) secondPartBounds.getHeight();
        }
    }

    private static Font chooseFont(String string) {
        Font font = NORMAL_FONT;
        if (font.canDisplayUpTo(string) != -1) {
            font = JAPANESE_FONT;
            if (font.canDisplayUpTo(string) != -1) {
                font = KOREAN_FONT;
                if (font.canDisplayUpTo(string) != -1) {
                    font = EMOJI_FONT;
                }
            }
        }
        return font;
    }

}
