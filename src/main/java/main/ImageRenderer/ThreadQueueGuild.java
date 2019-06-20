package main.ImageRenderer;


import DAO.Entities.UrlCapsule;
import main.ImageRenderer.Stealing.GaussianFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadQueueGuild extends ThreadQueue {


	public ThreadQueueGuild(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations) {
		super(queue, g, x, y, iterations);
	}


	@Override
	public void drawNames(UrlCapsule capsule, int y, int x, Graphics2D g, int imageWidth) {


		float[] rgb2 = new float[3];
		int a = Color.GRAY.getRGB();
		new Color(a).getRGBColorComponents(rgb2);
		Color colorB = new Color(rgb2[0], rgb2[1], rgb2[2], 0.4f);
		g.setColor(colorB);
		g.fillRect(0, 300 - 75, 300, 75);
		BufferedImage image = new BufferedImage(300, 75, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = image.createGraphics();
		g2.setColor(colorB);
		g2.fillRect(0, 0, 300, 75);
		g.drawImage(image, new GaussianFilter(10), 0, 225);
		g.setColor(Color.WHITE);

		String artistName = capsule.getArtistName();
		String plays = String.valueOf(capsule.getPlays());
		fontSize1 = 28;
		fontSize2 = 28;

		Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
		Font albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
		g.setFont(artistFont);

		int artistWidth = g.getFontMetrics().stringWidth(artistName);
		g.setFont(albumFont);

		int albumWidth = g.getFontMetrics().stringWidth(plays + " plays");

		while (artistWidth > imageWidth && fontSize1-- > 16) {
			artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
			g.setFont(artistFont);
			artistWidth = g.getFontMetrics().stringWidth(artistName);
		}
		while (albumWidth > imageWidth && fontSize2-- > 16) {
			fontSize2--;
			albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
			g.setFont(albumFont);
			albumWidth = g.getFontMetrics().stringWidth(plays + " plays");
		}

		g.setFont(artistFont);
		FontMetrics metric = g.getFontMetrics();
		int accum = metric.getAscent() - metric.getDescent() - metric.getLeading();

		g.drawString(capsule.getArtistName(), 150 - artistWidth / 2, 240 + accum);

		g.setFont(albumFont);
		metric = g.getFontMetrics();
		accum += metric.getAscent() - metric.getDescent() - metric.getLeading() + 1;
		g.drawString(plays + " plays", 150 - albumWidth / 2, 250 + accum);
		fontSize1 = START_FONT_SIZE;
		fontSize2 = START_FONT_SIZE;

	}

}



