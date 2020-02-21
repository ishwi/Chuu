package core.imagerenderer;


import core.imagerenderer.stealing.GaussianFilter;
import dao.entities.UrlCapsule;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class ThreadQueueGuild extends ThreadQueue {


    ThreadQueueGuild(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations) {
        super(queue, g, x, y, iterations);
    }


    @Override
    public void drawNames(UrlCapsule capsule, int y, int x, Graphics2D g, int imageWidth, BufferedImage image2) {

        float[] rgb2 = new float[3];
        int a = Color.GRAY.getRGB();
        new Color(a).getRGBColorComponents(rgb2);
        Color colorB = new Color(rgb2[0], rgb2[1], rgb2[2], 0.4f);
        g.setColor(colorB);
        g.fillRect(x * imageWidth, y * imageWidth - 75, 300, 75);
        BufferedImage image = new BufferedImage(300, 75, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = image.createGraphics();
        g2.setColor(colorB);
        g2.fillRect(0, 0, 300, 75);
        g.drawImage(image, new GaussianFilter(10), x * imageWidth, y * imageWidth + 225);
        g.setColor(Color.WHITE);

        String artistName = capsule.getArtistName();
        String plays = String.valueOf(capsule.getPlays());
        int fontSize1 = 28;
        int fontSize2 = 28;

        Font artistFont = new Font("Roboto", Font.PLAIN, fontSize1);
        Font albumFont = new Font("Roboto", Font.PLAIN, fontSize2);
        g.setFont(artistFont);

        int artistWidth = g.getFontMetrics().stringWidth(artistName);
        g.setFont(albumFont);

        int albumWidth = g.getFontMetrics().stringWidth(plays + " plays");

        while (artistWidth > imageWidth && fontSize1-- > 16) {
            artistFont = new Font("Roboto", Font.PLAIN, fontSize1);
            g.setFont(artistFont);
            artistWidth = g.getFontMetrics().stringWidth(artistName);
        }
        while (albumWidth > imageWidth && fontSize2-- > 16) {
            fontSize2--;
            albumFont = new Font("Roboto", Font.PLAIN, fontSize2);
            g.setFont(albumFont);
            albumWidth = g.getFontMetrics().stringWidth(plays + " plays");
        }

        g.setFont(artistFont);
        FontMetrics metric = g.getFontMetrics();
        int accum = metric.getAscent() - metric.getDescent() - metric.getLeading();

        g.drawString(capsule.getArtistName(), x * imageWidth + 150 - artistWidth / 2, y * imageWidth + 240 + accum);

        g.setFont(albumFont);
        metric = g.getFontMetrics();
        accum += metric.getAscent() - metric.getDescent() - metric.getLeading() + 1;
        g.drawString(plays + " plays", x * imageWidth + 150 - albumWidth / 2, y * imageWidth + 250 + accum);

    }

}



