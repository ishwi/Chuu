package core.imagerenderer;


import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.AlbumUserPlays;
import dao.entities.ArtistAlbums;
import dao.entities.ReturnNowPlaying;
import dao.entities.WrapperReturnNowPlaying;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BandRendered {

    private static final int X_MAX = 1500;
    private static final int Y_MAX = 1000;
    private static final int X_MARGIN = 25;
    private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 32);
    private static final Font DESC_FONT = new Font("Noto Sans CJK JP Light", Font.PLAIN, 32);
    private static final StringFitter fitter = new StringFitterBuilder(32, 300)
            .setBaseFont(NORMAL_FONT)
            .setMinSize(8).build();
    private static final int albumsStartPosition = X_MARGIN + 400 + 195 + 40;
    private static final int albumsStartPositionSmall = X_MARGIN + 355 + 195 + 52;


    private BandRendered() {

    }

    public static BufferedImage makeBandImage(WrapperReturnNowPlaying wrapperReturnNowPlaying, ArtistAlbums ai, int plays, BufferedImage logo, String user, long threshold) {
        BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
        BufferedImage lastFmLogo;
        BufferedImage artistReplacement;

        String artist = wrapperReturnNowPlaying.getArtist();

        //Loads logo if it were to exist
        try (InputStream in = BandRendered.class.getResourceAsStream("/images/logo2.png")) {
            InputStream input = Objects.requireNonNull(in);
            lastFmLogo = ImageIO.read(input);
            lastFmLogo = Scalr.resize(lastFmLogo, 30);
        } catch (IOException e) {
            lastFmLogo = null;
        }
        artistReplacement = GraphicUtils.noArtistImage;

        BufferedImage artistImageFill = GraphicUtils
                .getImageFromUrl(wrapperReturnNowPlaying.getUrl(), artistReplacement);
        //Inits the background with the blurred image
        Graphics2D g = GraphicUtils.initArtistBackground(canvas, artistImageFill);

        Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
        Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));
        Color fontColor = GraphicUtils.getBetter(GraphicUtils.getFontColorBackground(canvas));

        g.setColor(fontColor);


        List<AlbumUserPlays> albumUserPlaysList = ai.getAlbumList();
        List<BufferedImage> albumsImages = new ArrayList<>();
        for (int i = 0, albumUserPlaysListSize = albumUserPlaysList.size(); i < albumUserPlaysListSize && i < 9; i++) {
            AlbumUserPlays albumUserPlays = albumUserPlaysList.get(i);
            BufferedImage image = GraphicUtils.getImage(albumUserPlays.getAlbumUrl());
            if (image == null) {
                image = GraphicUtils.noArtistImage;
            }
            if (albumUserPlays.getPlays() > threshold) {
                albumsImages.add(image);
            }
        }

        g.setFont(NORMAL_FONT);

        FontMetrics metrics = g.getFontMetrics();
        String people = "Top 5 people";
        int width = metrics.stringWidth(people);
        GraphicUtils.drawStringNicely(g, people, (X_MARGIN + 40) + (380 / 2) - width / 2, 700 - metrics
                .getAscent(), canvas);
        GraphicUtils
                .doChart(g, X_MARGIN + 40, 700 - 20, 400, 50, 5, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, DESC_FONT
                        .deriveFont(36f));

        int count = 0;
        int imagesDrawn = 0;

        int imageSize = albumsImages.size() > 4 ? 210 : 300;
        int size = albumsImages.size();
        for (BufferedImage albumsImage : albumsImages) {
            int posX;

            Point point = drawImage(count++, size);
            g.drawImage(albumsImage, point.x, point.y, imageSize, imageSize, null);
            int baseline = point.y + imageSize;
            AlbumUserPlays albumUserPlays = albumUserPlaysList.get(count - 1);
            String album = albumUserPlays.getAlbum();
            String play = Integer.toString(albumUserPlays.getPlays());

            Font ogFont = g.getFont();
            float sizeFont = ogFont.getSize();

            StringFitter.FontMetadata albumFont = fitter
                    .getFontMetadata(g, album, imageSize);
            width = (int) albumFont.bounds().getWidth();
            GraphicUtils
                    .drawStringNicely(g, albumFont, point.x + (imageSize / 2) - width / 2, baseline + metrics.getAscent(), canvas);
            g.setFont(ogFont);

            baseline += metrics.getAscent() + metrics.getDescent();
            width = metrics.stringWidth(play);
            int start = point.x + (imageSize / 2) - width / 2;
            int finish = start + width;
            width += 25;

            GraphicUtils
                    .drawStringNicely(g, play, point.x + (imageSize / 2) - width / 2, baseline + metrics.getAscent(), canvas);
            g.drawImage(lastFmLogo, finish, baseline + metrics.getAscent() - metrics.getDescent() - metrics
                    .getLeading() - 8, null);
            imagesDrawn++;
        }

        int yBaseLine = 380;
        if (artistImageFill != null) {
            g.drawImage(GraphicUtils.resizeOrCrop(artistImageFill, 380), X_MARGIN + 40 + (400 - 380) / 2, 25, null);
        }
        yBaseLine += metrics.getAscent() + metrics.getDescent() + metrics.getLeading() + 20;
        StringFitter.FontMetadata fontMetadata = new StringFitterBuilder(g.getFont().getSize(), 380)
                .setBaseFont(g.getFont())
                .build().getFontMetadata(g, artist);
        GraphicUtils.drawStringNicely(g, fontMetadata, (int) (X_MARGIN + 40 + (380 / 2) - fontMetadata.bounds().getWidth() / 2), yBaseLine, canvas);


        ReturnNowPlaying myRow = new ReturnNowPlaying(1, user, artist, plays);
        myRow.setDiscordName(user);

        WrapperReturnNowPlaying wrapper1Row = new WrapperReturnNowPlaying(Collections
                .singletonList(myRow), 1, artist, artist);
        GraphicUtils.doChart(g, X_MARGIN + 40, yBaseLine + metrics
                .getAscent() - 20, 400, 50, 1, wrapper1Row, colorB1, colorB, lastFmLogo, false, DESC_FONT
                .deriveFont(36f));

        return canvas;
    }

    public static Point drawImage(int index, int total) {
        int totalWidthSmall = X_MAX - albumsStartPositionSmall;
        int fitOne = (totalWidthSmall - 300) / 2;
        int fitTwo = (totalWidthSmall - 550) / 2;
        return switch (total) {
            case 1 -> new Point(albumsStartPosition + 175, 105 + 200);
            case 2 -> new Point(albumsStartPosition + 175, 105 + 400 * index);
            case 3 -> {
                int[] pos = {20, 370, 175};
                yield new Point(albumsStartPosition + pos[index], 105 + 400 * (index / 2));
            }
            case 4 -> new Point(albumsStartPosition + 350 * (index % 2), 105 + 400 * (index / 2));
            case 5 -> {
                if (index < 3) {
                    yield new Point(albumsStartPositionSmall + 275 * (index % 3), 205);
                } else {
                    yield new Point(albumsStartPositionSmall + (fitTwo + 250 * (index - 3)), 205 + 300);
                }
            }
            case 6 -> new Point(albumsStartPositionSmall + 275 * (index % 3), 205 + 300 * ((index) / 3));
            case 7 -> {
                if (index == 6) {
                    yield new Point(albumsStartPositionSmall + fitOne, 55 + 300 * 2);
                }
                yield new Point(albumsStartPositionSmall + 275 * (index % 3), 55 + 300 * ((index) / 3));
            }
            case 8 -> {
                if (index >= 6) {
                    yield new Point(albumsStartPositionSmall + (fitTwo + 250 * (index - 6)), 55 + 300 * 2);
                }
                yield new Point(albumsStartPositionSmall + 275 * (index % 3), 55 + 300 * ((index) / 3));
            }
            case 9 -> new Point(albumsStartPositionSmall + 275 * (index % 3), 55 + 300 * ((index) / 3));
            default -> throw new IllegalStateException();
        };
    }
}
