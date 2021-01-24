package core.imagerenderer;

import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.ProfileEntity;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ProfileMaker {
    private static final int X_SIZE = 1920;
    private static final int Y_SIZE = 1080;
    private static final int TEXT_END = 450 + 45;
    private static final int IMAGE_START = X_SIZE - TEXT_END + 55;
    private static final int ARTIST_IMAGES_SIZE = 300;
    private static final int AVATAR_SIZE = 250;
    private static final int DEFAULT_FONT = 44;
    private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 44);
    private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP Regular", Font.PLAIN, 44);

    private ProfileMaker() {

    }

    public static BufferedImage makeProfile(ProfileEntity entity) {

        BufferedImage image = new BufferedImage(X_SIZE, Y_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        GraphicUtils.setQuality(g);
        GraphicUtils.initRandomImageBlurredBackground(g, X_SIZE, Y_SIZE);
        BufferedImage imageToDraw;

        Font font = GraphicUtils.chooseFont(" ");
        final Font ogFont = font.deriveFont((float) DEFAULT_FONT);

        //CrownImage
        imageToDraw = GraphicUtils.getImageFromUrl(entity.getCrownUrl(), GraphicUtils.noArtistImage);
        int baseline = 115;
        makeDrawingStringProcess("Top Crown", g, image, ogFont, IMAGE_START - 25, 350, baseline);
        g.drawImage(Scalr.resize(imageToDraw, ARTIST_IMAGES_SIZE), IMAGE_START, 175, null);
        baseline += 355;
        makeDrawingStringProcess(entity
                .getCrownArtist(), g, image, ogFont, IMAGE_START - 25, 350, baseline);
        baseline += 95;

        //UniqueImage
        imageToDraw = GraphicUtils.getImageFromUrl(entity.getUniqueUrl(), GraphicUtils.noArtistImage);
        makeDrawingStringProcess("Top Unique", g, image, ogFont, IMAGE_START - 25, 350, baseline);
        baseline += 55;
        g.drawImage(Scalr
                .resize(imageToDraw, ARTIST_IMAGES_SIZE), IMAGE_START, baseline, null);
        baseline += 300;
        makeDrawingStringProcess(entity.getUniqueArtist(), g, image, ogFont, IMAGE_START - 25, 350, baseline);

        //AvatarImage
        imageToDraw = GraphicUtils.getImageFromUrl(entity.getLastfmUrl(), null);
        if (imageToDraw == null) {
            imageToDraw = GraphicUtils.getImageFromUrl(entity.getDiscordUrl(), GraphicUtils.noArtistImage);
        }

        g.drawImage(Scalr.resize(imageToDraw, AVATAR_SIZE), (X_SIZE - AVATAR_SIZE) / 2 - 350, 50, null);

        g.setFont(ogFont.deriveFont(64f));

        GraphicUtils.drawStringNicely(g, entity
                .getUsername(), (X_SIZE - AVATAR_SIZE) / 2 - 350 + AVATAR_SIZE + 20, 50 + ((AVATAR_SIZE + g
                .getFontMetrics().getAscent()) / 2), image);

        String s;
        int width;
        g.setFont(ogFont.deriveFont(54f));
        int increment = (int) ((double) g.getFontMetrics().getMaxAscent() * 1.5);
        baseline = 425;

        GraphicUtils.drawStringNicely(g, "Total Number of scrobbles", 25, baseline, image);
        s = String.valueOf(entity.getScrobbles());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Total Number of albums", 25, baseline, image);
        s = String.valueOf(entity.getAlbums());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Total Number of artists", 25, baseline, image);
        s = String.valueOf(entity.getArtist());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Total Number of crowns", 25, baseline, image);
        s = String.valueOf(entity.getCrowns());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Total Number of unique artists", 25, baseline, image);
        s = String.valueOf(entity.getUniques());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);

        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Total random urls submitted", 25, baseline, image);
        s = String.valueOf(entity.getRandomCount());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;


        GraphicUtils.drawStringNicely(g, "Total Number of commands executed", 25, baseline, image);
        s = String.valueOf(entity.getCommandStats().commandCount());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Total Number of images submitted", 25, baseline, image);
        s = String.valueOf(entity.getCommandStats().imageCount());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);


        g.dispose();
        return image;

    }

    private static void makeDrawingStringProcess(String string, Graphics2D g, BufferedImage image, Font ogFont, int xStartingPoint, int widthFit, int ySTARTINGPOINT) {
        StringFitter.FontMetadata fontMetadata = new StringFitterBuilder(DEFAULT_FONT, widthFit)
                .setMinSize(14).build()
                .getFontMetadata(g, string);
        int width = (int) fontMetadata.bounds().getWidth();
        FontMetrics fontMetrics = g.getFontMetrics();
        GraphicUtils
                .drawStringNicely(g, fontMetadata, xStartingPoint + (widthFit / 2) - width / 2, ySTARTINGPOINT + fontMetrics
                        .getAscent(), image);
        g.setFont(ogFont);
    }
}
