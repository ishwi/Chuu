package core.imagerenderer;

import core.Chuu;
import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.FullAlbumEntity;
import dao.entities.Track;
import dao.entities.UserInfo;
import dao.exceptions.ChuuServiceException;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

import static core.imagerenderer.GraphicUtils.chooseFont;
import static core.imagerenderer.GraphicUtils.ran;

public class TrackDistributor {
    private static final int TILE_SIZE = 45;
    private static final int WIDTH_CONSTANT = 935;
    private static final int HEIGHT_CONSTANT = 390;
    private static final int HEIGHT_BOTTOM = 0;
    private static final int FONT_SIZE = 30;
    private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, FONT_SIZE);


    private static final BufferedImage template;
    private static final BufferedImage noalbumImage;
    private static final List<Color> lightPalettes;
    private static final StringFitter trackFitter = new StringFitterBuilder(FONT_SIZE, 400)
            .setMinSize(8)
            .setBaseFont(NORMAL_FONT)
            .build();

    static {

        noalbumImage = GraphicUtils.noArtistImage;

        try (InputStream in = BandRendered.class.getResourceAsStream("/images/template.png")) {
            template = ImageIO.read(in);
        } catch (IOException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException();
        }
        lightPalettes = initLightPalettes();
    }

    private static List<Color> initLightPalettes() {
        List<Color> colors = new ArrayList<>();
        colors.add(Color.decode("#769fcd"));
        colors.add(Color.decode("#b9d7ea"));
        colors.add(Color.decode("#d6e6f2"));
        colors.add(Color.decode("#f7fbfc"));

        colors.add(Color.decode("#ffcfdf"));
        colors.add(Color.decode("#fefdca"));
        colors.add(Color.decode("#e0f9b5"));
        colors.add(Color.decode("#a5dee5"));

        colors.add(Color.decode("#e4f9f5"));
        colors.add(Color.decode("#30e3ca"));
        colors.add(Color.decode("#11999e"));
        colors.add(Color.decode("#40514e"));

        colors.add(Color.decode("#f9ecec"));
        colors.add(Color.decode("#f0d9da"));
        colors.add(Color.decode("#c8d9eb"));
        colors.add(Color.decode("#ecf2f9"));

        colors.add(Color.decode("#ffe6eb"));
        colors.add(Color.decode("#defcfc"));
        colors.add(Color.decode("#cbf1f5"));
        colors.add(Color.decode("#a6e3e9"));

        colors.add(Color.decode("#ececec"));
        colors.add(Color.decode("#9fd3c7"));
        colors.add(Color.decode("#385170"));
        colors.add(Color.decode("#142d4c"));

        return colors;
    }

    public static BufferedImage drawImage(FullAlbumEntity fae) {
        List<Track> trackList = fae.getTrackList();
        int trackCount = trackList.size();

        Optional<Track> max = trackList.stream().max(Comparator.comparingInt(Track::getPlays));
        assert max.isPresent();

        int maxList = max.get().getPlays();
        if (maxList == 0) {
            maxList = 1;
        }


        //Background image set up
        BufferedImage artistImageFill = GraphicUtils
                .getImageFromUrl(fae.getArtistUrl(), null);

        //Main Image
        BufferedImage dist = new BufferedImage(WIDTH_CONSTANT, HEIGHT_CONSTANT + HEIGHT_BOTTOM + (TILE_SIZE) * trackCount + 15, BufferedImage.TYPE_INT_ARGB);
        //Album Image
        BufferedImage albumImage = GraphicUtils.getImageFromUrl(fae.getAlbumUrl(), noalbumImage);

        //Image Artist
        Graphics2D g = GraphicUtils.initArtistBackground(dist, artistImageFill);

        g.setFont(NORMAL_FONT);

        //Upper Template Part
        g.drawImage(template, 0, 0, null);

        GraphicUtils.drawStringNicely(g, fae.getArtist(), 397, 160, dist);
        GraphicUtils.drawStringNicely(g, "Artist", 397, 199, dist);

        GraphicUtils.drawStringNicely(g, fae.getAlbum(), 397, 235, dist);
        GraphicUtils.drawStringNicely(g, "Album", 397, 274, dist);

        g.drawImage(Scalr.resize(albumImage, 330, 330), 22, 22, null);

        doHistContent(g, maxList, dist, trackList, 905, HEIGHT_CONSTANT);

        g.dispose();
        return dist;


    }

    public static BufferedImage drawImageMirrored(FullAlbumEntity first, FullAlbumEntity second, UserInfo firstInfo, UserInfo secondInfo) {
        List<Track> firstTrackList = first.getTrackList();
        List<Track> secondTrackList = second.getTrackList();
        int trackCount = Math.max(firstTrackList.size(), secondTrackList.size());

        Optional<Track> max = Stream.concat(firstTrackList.stream(), secondTrackList.stream()).max(Comparator.comparingInt(Track::getPlays));
        assert max.isPresent();

        int maxList = max.get().getPlays();
        if (maxList == 0) {
            maxList = 1;
        }


        //Background image set up
        BufferedImage artistImageFill = GraphicUtils
                .getImageFromUrl(first.getArtistUrl(), null);

        //Main Image
        int mirroredWidth = WIDTH_CONSTANT * 2 + 371;
        BufferedImage dist = new BufferedImage(mirroredWidth, HEIGHT_CONSTANT + HEIGHT_BOTTOM + (TILE_SIZE) * trackCount + 15 + 20, BufferedImage.TYPE_INT_ARGB);


        //Album Image

        BufferedImage userImage = GraphicUtils.getImageFromUrl(firstInfo.getImage(), noalbumImage);
        BufferedImage userImage2 = GraphicUtils.getImageFromUrl(secondInfo.getImage(), noalbumImage);

        //Image Artist
        Graphics2D g = GraphicUtils.initArtistBackground(dist, artistImageFill);
        Font firstUserFont = chooseFont(firstInfo.getUsername());
        g.setFont(firstUserFont.deriveFont(30f));
        Rectangle2D f = GraphicUtils.fitAndGetBounds(firstInfo.getUsername(), g, 330, 18f);
        firstUserFont = g.getFont();
        Font secondUserFont = chooseFont(secondInfo.getUsername());
        g.setFont(secondUserFont.deriveFont(30f));
        Rectangle2D n = GraphicUtils.fitAndGetBounds(secondInfo.getUsername(), g, 330, 18f);
        secondUserFont = g.getFont();

        int baseline = (int) Math.max(f.getHeight(), n.getHeight());
        g.setFont(firstUserFont);
        GraphicUtils.drawStringNicely(g, firstInfo.getUsername(), (int) (22 + (330 / 2 - f.getWidth() / 2)), 22 + 330 + baseline, dist);
        g.setFont(secondUserFont);
        GraphicUtils.drawStringNicely(g, secondInfo.getUsername(), (int) (mirroredWidth - 22 - 330 + ((330 / 2 - n.getWidth() / 2))), 22 + 330 + baseline, dist);
        g.setFont(NORMAL_FONT);

        //Upper Template Part
        g.drawImage(template, 0, 0, null);
        g.drawImage(Scalr.rotate(template, Scalr.Rotation.FLIP_HORZ), mirroredWidth - (template.getWidth()), 0, null);
        BufferedImage crop = Scalr.crop(template, 0, 0, 370, 390);
        g.drawImage(crop, WIDTH_CONSTANT, 0, null);
        BufferedImage albumImage = GraphicUtils.getImageFromUrl(first.getAlbumUrl(), noalbumImage);


        int i = g.getFontMetrics().stringWidth(first.getArtist());
        GraphicUtils.drawStringNicely(g, first.getArtist(), 397, 160, dist);
        GraphicUtils.drawStringNicely(g, first.getArtist(), mirroredWidth - 397 - i, 160, dist);

        i = g.getFontMetrics().stringWidth("Artist");
        GraphicUtils.drawStringNicely(g, "Artist", 397, 199, dist);
        GraphicUtils.drawStringNicely(g, "Artist", mirroredWidth - 397 - i, 199, dist);

        i = g.getFontMetrics().stringWidth(first.getAlbum());
        GraphicUtils.drawStringNicely(g, first.getAlbum(), 397, 235, dist);
        GraphicUtils.drawStringNicely(g, first.getAlbum(), mirroredWidth - 397 - i, 235, dist);
        i = g.getFontMetrics().stringWidth("Album");
        GraphicUtils.drawStringNicely(g, "Album", 397, 274, dist);
        GraphicUtils.drawStringNicely(g, "Album", mirroredWidth - 397 - i, 274, dist);

        g.drawImage(Scalr.resize(userImage, 330, 330), 22, 22, null);
        g.drawImage(Scalr.resize(userImage2, 330, 330), mirroredWidth - 22 - 330, 22, null);
        g.drawImage(Scalr.resize(albumImage, 330, 330), WIDTH_CONSTANT + 22, 22, null);


        doHistContent(g, maxList, dist, firstTrackList, 905 + 370 / 2, HEIGHT_CONSTANT + 20);
        doHistContentReversed(g, maxList, dist, secondTrackList, 905 + 370 / 2, HEIGHT_CONSTANT + 20);

        g.dispose();
        return dist;

    }

    private static void doHistContentReversed(Graphics2D g, int maxList, BufferedImage dist, List<Track> trackList, int widthBarsSpace, int starttingY) {
        Font ogFont;
        Font font;

        int xLimit = dist.getWidth();
        int yLimit = dist.getHeight() - HEIGHT_CONSTANT;

        Color color = lightPalettes.get(new Random().nextInt(lightPalettes.size() - 1));
        //Get a estimate of the average colour of the background
        Color[] a = new Color[15];
        for (int i = 0; i < 15; i++) {
            int rgb = dist.getRGB(ran.nextInt(xLimit), ran.nextInt(yLimit) + HEIGHT_CONSTANT);
            a[i] = (new Color(rgb));
        }
        Color betterCollection = GraphicUtils.getBetter(a);
        if (betterCollection.equals(Color.white))
            color = color.darker().darker();

        //I like transparency
        g.setColor(GraphicUtils.makeMoreTransparent(color, 0.7f));

        int startingPoint = starttingY;
        OptionalInt max = trackList.stream().map(Track::getName)
                .mapToInt(x -> g.getFontMetrics().stringWidth(x))
                .max();
        int realMax = Math.min(452, max.orElse(400));
        int extra = 45;

        int minimunAmmount = realMax + extra;
        for (Track track : trackList) {
            float fontSize = FONT_SIZE;


            StringFitter.FontMetadata fontMetadata = trackFitter.getFontMetadata(g, track.getName());


            int rectWidth = (int) (minimunAmmount + (widthBarsSpace - minimunAmmount) * (float) track.getPlays() / maxList);
            int i1 = WIDTH_CONSTANT * 2 + 370;
            g.fillRect(i1 - 15 - rectWidth, startingPoint, rectWidth, 38);

            int i = g.getFontMetrics(fontMetadata.maxFont()).stringWidth(track.getName());
            GraphicUtils.drawStringNicely(g, fontMetadata, i1 - 25 - i, startingPoint +
                            (TILE_SIZE - 7 - g.getFontMetrics(fontMetadata.maxFont()).getHeight()) / 2 + g.getFontMetrics(fontMetadata.maxFont()).getAscent()
                    , dist);

            String plays = String.valueOf(track.getPlays());
            ogFont = g.getFont();
            g.setFont(fontMetadata.maxFont());
            GraphicUtils.drawStringNicely(g, plays, i1 - 15 - rectWidth + 5, startingPoint +
                            (TILE_SIZE - 7 - g.getFontMetrics(fontMetadata.maxFont()).getHeight()) / 2 + g.getFontMetrics(fontMetadata.maxFont()).getAscent()
                    , dist);
            startingPoint += TILE_SIZE;
            g.setFont(ogFont);

        }
    }

    private static void doHistContent(Graphics2D g, int maxList, BufferedImage dist, List<Track> trackList, int widthBarsSpace, int yStart) {
        Font ogFont;
        Font font;

        int xLimit = dist.getWidth();
        int yLimit = dist.getHeight() - HEIGHT_CONSTANT;

        Color color = lightPalettes.get(new Random().nextInt(lightPalettes.size() - 1));
        //Get a estimate of the average colour of the background
        Color[] a = new Color[15];
        for (int i = 0; i < 15; i++) {
            int rgb = dist.getRGB(ran.nextInt(xLimit), ran.nextInt(yLimit) + HEIGHT_CONSTANT);
            a[i] = (new Color(rgb));
        }
        Color betterCollection = GraphicUtils.getBetter(a);
        if (betterCollection.equals(Color.white))
            color = color.darker().darker();

        //I like transparency
        g.setColor(GraphicUtils.makeMoreTransparent(color, 0.7f));

        int startingPoint = yStart;
        OptionalInt max = trackList.stream().map(Track::getName)
                .mapToInt(x -> g.getFontMetrics().stringWidth(x))
                .max();
        int realMax = Math.min(452, max.orElse(400));
        int extra = 45;

        int minimunAmmount = realMax + extra;
        for (Track track : trackList) {
            float fontSize = FONT_SIZE;

            StringFitter.FontMetadata fontMetadata = trackFitter.getFontMetadata(g, track.getName());

            int rectWidth = (int) (minimunAmmount + (widthBarsSpace - minimunAmmount) * (float) track.getPlays() / maxList);
            g.fillRect(15, startingPoint, rectWidth, 38);

            GraphicUtils.drawStringNicely(g, fontMetadata, 25, startingPoint +
                            (TILE_SIZE - 7 - g.getFontMetrics(fontMetadata.maxFont()).getHeight()) / 2 + g.getFontMetrics(fontMetadata.maxFont()).getAscent()
                    , dist);
            String plays = String.valueOf(track.getPlays());
            ogFont = g.getFont();
            g.setFont(fontMetadata.maxFont());
            GraphicUtils.drawStringNicely(g, plays, 15 + rectWidth - g.getFontMetrics()
                            .stringWidth(plays) - 5, startingPoint +
                            (TILE_SIZE - 7 - g.getFontMetrics(fontMetadata.maxFont()).getHeight()) / 2 + g.getFontMetrics(fontMetadata.maxFont()).getAscent()
                    , dist);
            startingPoint += TILE_SIZE;
            g.setFont(ogFont);

        }

    }

}
