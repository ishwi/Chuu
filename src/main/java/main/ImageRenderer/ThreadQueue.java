package main.ImageRenderer;

import DAO.Entities.UrlCapsule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

//
//BufferedImage result = new BufferedImage(
//        1500   ,1500, //work these out
//        BufferedImage.TYPE_INT_RGB);
//        Graphics g = result.getGraphics();
class ThreadQueue implements Runnable {
	private final BlockingQueue<UrlCapsule> queue;
	private final Graphics2D g;
	private final int y;
	private final int x;
	private final AtomicInteger iters;
	private final int START_FONT_SIZE = 24;
	private int fontSize1 = START_FONT_SIZE;
	private int fontSize2 = START_FONT_SIZE;


	public ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iters) {
		this.queue = queue;
		this.g = g;
		this.x = x;
		this.y = y;
		this.iters = iters;

	}

	@Override
	public void run() {
		System.out.println("Value of thread " + this.hashCode() + " : " + iters.get());
		while (iters.getAndDecrement() > 0) {

			Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, 24);
			g.setFont(artistFont);
			g.setColor(Color.BLACK);
			int fontHeight = g.getFontMetrics().getHeight();
			int fontWidth = (int) (fontHeight * 0.45);


			try {
				UrlCapsule encapsuler = queue.take();
				BufferedImage image;
				URL url;
				try {

					int pos = encapsuler.getPos();
					int y = (int) Math.floor(pos / this.x);
					int x = pos % this.x;

					url = new URL(encapsuler.getUrl());
					image = ImageIO.read(url);

					drawImage(image, encapsuler);
					g.drawImage(image, x * 300, y * 300, null);
					System.out.println(x + "                " + y);


				} catch (Exception e) {
					e.printStackTrace();
					Color temp = g.getColor();
					g.setColor(Color.WHITE);
					int pos = encapsuler.getPos();
					int y = (int) Math.floor(pos / this.x);
					int x = pos % this.x;
					g.fillRect(x * 300, y * 300, 300, 300);
					g.setColor(Color.BLACK);

					drawNames(encapsuler, y, x, g);
					g.setColor(temp);
					e.printStackTrace();
					System.out.println("\n JAJAJAJa\n" + encapsuler.getUrl() + encapsuler.getPos() + "\n \n ");
				}


			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("\n \n \n\n\n\n\n\n\n           THREAD FINISHED             \n \n \n\n\n\n\n\n\n");

	}

	private void drawImage(BufferedImage image, UrlCapsule encapsuler) {
		int a = image.getRGB(0, 0);
		Color mycolor = new Color(a);
		Graphics2D gtemp = image.createGraphics();
		GraphicUtils.setQuality(gtemp);
		gtemp.setColor(getBetter(mycolor));
		drawNames(encapsuler, 0, 0, gtemp);
		gtemp.dispose();
	}

	private void drawNames(UrlCapsule encapsuler, int y, int x, Graphics2D grap) {
		String artistName = encapsuler.getArtistName();
		String albumName = encapsuler.getAlbumName();

		Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
		Font albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
		grap.setFont(artistFont);

		int artistWidth = grap.getFontMetrics().stringWidth(artistName);
		grap.setFont(albumFont);

		int albumWidth = grap.getFontMetrics().stringWidth(albumName);

		while (artistWidth > 300 && fontSize1-- > 14) {
			artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
			grap.setFont(artistFont);
			artistWidth = grap.getFontMetrics().stringWidth(artistName);
		}
		while (albumWidth > 300 && fontSize2-- > 14) {
			fontSize2--;
			albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
			grap.setFont(albumFont);
			albumWidth = grap.getFontMetrics().stringWidth(albumName);
		}

		grap.setFont(artistFont);
		FontMetrics metric = grap.getFontMetrics();
		int accu = metric.getAscent() - metric.getDescent() - metric.getLeading();
		grap.drawString(encapsuler.getArtistName(), x * 300, y * 300 + accu);

		grap.setFont(albumFont);
		metric = grap.getFontMetrics();
		accu += metric.getAscent() - metric.getDescent() - metric.getLeading() + 1;
		grap.drawString(encapsuler.getAlbumName(), x * 300, y * 300 + accu);
		fontSize1 = START_FONT_SIZE;
		fontSize2 = START_FONT_SIZE;

	}

	private Color getBetter(Color color) {
		Double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
		return y < 128 ? Color.WHITE : Color.BLACK;

	}

}
