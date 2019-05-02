package main.ImageRenderer;


import DAO.Entities.UrlCapsule;
import main.ImageRenderer.Stealing.GaussianFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadQueueGuild extends ThreadQueue {


	public ThreadQueueGuild(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iters) {
		super(queue, g, x, y, iters);
	}


	@Override
	public void drawNames(UrlCapsule encapsuler, int y, int x, Graphics2D grap) {


		float[] rgb2 = new float[3];
		int a = Color.GRAY.getRGB();
		new Color(a).getRGBColorComponents(rgb2);
		Color colorB = new Color(rgb2[0], rgb2[1], rgb2[2], 0.4f);
		grap.setColor(colorB);
		grap.fillRect(0, 300 - 75, 300, 75);
		BufferedImage image = new BufferedImage(300, 75, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = image.createGraphics();
		g2.setColor(colorB);
		g2.fillRect(0, 0, 300, 75);
		grap.drawImage(image, new GaussianFilter(10), 0, 225);
		grap.setColor(Color.black);

		String artistName = encapsuler.getArtistName();
		String plays = String.valueOf(encapsuler.getPlays());
		fontSize1 = 28;
		fontSize2 = 28;

		Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
		Font albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
		grap.setFont(artistFont);

		int artistWidth = grap.getFontMetrics().stringWidth(artistName);
		grap.setFont(albumFont);

		int albumWidth = grap.getFontMetrics().stringWidth(plays + " plays");

		while (artistWidth > 300 && fontSize1-- > 16) {
			artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
			grap.setFont(artistFont);
			artistWidth = grap.getFontMetrics().stringWidth(artistName);
		}
		while (albumWidth > 300 && fontSize2-- > 16) {
			fontSize2--;
			albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
			grap.setFont(albumFont);
			albumWidth = grap.getFontMetrics().stringWidth(plays + " plays");
		}

		grap.setFont(artistFont);
		FontMetrics metric = grap.getFontMetrics();
		int accu = metric.getAscent() - metric.getDescent() - metric.getLeading();

		grap.drawString(encapsuler.getArtistName(), 150 - artistWidth / 2, 240 + accu);

		grap.setFont(albumFont);
		metric = grap.getFontMetrics();
		accu += metric.getAscent() - metric.getDescent() - metric.getLeading() + 1;
		grap.drawString(plays + " plays", 150 - albumWidth / 2, 250 + accu);
		fontSize1 = START_FONT_SIZE;
		fontSize2 = START_FONT_SIZE;

	}

}



