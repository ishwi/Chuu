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

        //TopArtist
        String topArtist;

        if (entity.topArtist() != null) {
            imageToDraw = GraphicUtils.getImageFromUrl(entity.topArtist().getUrl(), GraphicUtils.noArtistImage);
            topArtist = entity.topArtist().getArtist();

        } else {
            imageToDraw = GraphicUtils.noArtistImage;
            topArtist = "None";
        }

        int baseline = 115;
        makeDrawingStringProcess("Fav artist", g, image, ogFont, IMAGE_START - 25, 350, baseline);
        g.drawImage(Scalr.resize(imageToDraw, ARTIST_IMAGES_SIZE), IMAGE_START, 175, null);
        baseline += 355;


        makeDrawingStringProcess(topArtist, g, image, ogFont, IMAGE_START - 25, 350, baseline);
        baseline += 95;

        //UniqueImage
        String topAlbum;
        if (entity.topAlbum() != null) {
            imageToDraw = GraphicUtils.getImageFromUrl(entity.topAlbum().getUrl(), GraphicUtils.noArtistImage);
            topAlbum = entity.topAlbum().getAlbum();
        } else {
            imageToDraw = GraphicUtils.noArtistImage;
            topAlbum = "None";
        }

        makeDrawingStringProcess("Fav album", g, image, ogFont, IMAGE_START - 25, 350, baseline);
        baseline += 55;
        g.drawImage(Scalr
                .resize(imageToDraw, ARTIST_IMAGES_SIZE), IMAGE_START, baseline, null);
        baseline += 300;
        makeDrawingStringProcess(topAlbum, g, image, ogFont, IMAGE_START - 25, 350, baseline);

        //AvatarImage
        imageToDraw = GraphicUtils.getImageFromUrl(entity.imageUrl(), GraphicUtils.noArtistImage);

        g.drawImage(Scalr.resize(imageToDraw, AVATAR_SIZE), (X_SIZE - AVATAR_SIZE) / 2 - 350, 50, null);

        g.setFont(ogFont.deriveFont(64f));

        GraphicUtils.drawStringNicely(g, entity.lastmId()
                , (X_SIZE - AVATAR_SIZE) / 2 - 350 + AVATAR_SIZE + 20, 50 + ((AVATAR_SIZE + g
                        .getFontMetrics().getAscent()) / 2), image);

        String s;
        int width;
        g.setFont(ogFont.deriveFont(54f));
        int increment = (int) ((double) g.getFontMetrics().getMaxAscent() * 1.5);
        baseline = 425;

        GraphicUtils.drawStringNicely(g, "Scrobbles", 25, baseline, image);
        s = String.valueOf(entity.scrobbles());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Albums", 25, baseline, image);
        s = String.valueOf(entity.albums());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Artists", 25, baseline, image);
        s = String.valueOf(entity.artist());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Plays fav artist", 25, baseline, image);
        s = String.valueOf(entity.topArtist() != null ? entity.topArtist().getCount() : 0);
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Plays fav album", 25, baseline, image);
        s = String.valueOf(entity.topAlbum() != null ? entity.topAlbum().getCount() : 0);
        width = g.getFontMetrics(g.getFont()).stringWidth(s);

        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;


        GraphicUtils.drawStringNicely(g, "Commands executed", 25, baseline, image);
        s = String.valueOf(entity.commandStats().commandCount());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;

        GraphicUtils.drawStringNicely(g, "Images submitted", 25, baseline, image);
        s = String.valueOf(entity.commandStats().imageCount());
        width = g.getFontMetrics(g.getFont()).stringWidth(s);
        GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
        baseline += increment;


        GraphicUtils.drawStringNicely(g, "Random urls submitted", 25, baseline, image);
        s = String.valueOf(entity.randomCount());
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
