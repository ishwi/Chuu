package main.ImageRenderer;

import DAO.Entities.UrlCapsule;
import org.imgscalr.Scalr;

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
	final int START_FONT_SIZE = 24;
	private final AtomicInteger iterations;
	int fontSize1 = START_FONT_SIZE;
	int fontSize2 = START_FONT_SIZE;


	public ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations) {
		this.queue = queue;
		this.g = g;
		this.x = x;
		this.y = y;
		this.iterations = iterations;

	}

	@Override
	public void run() {
		while (iterations.getAndDecrement() > 0) {

			Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, 24);
			g.setFont(artistFont);
			g.setColor(Color.BLACK);
			int fontHeight = g.getFontMetrics().getHeight();


			try {
				UrlCapsule capsule = queue.take();
				BufferedImage image;
				URL url;
				try {

					int pos = capsule.getPos();
					int y = (int) Math.floor(pos / this.x);
					int x = pos % this.x;

					url = new URL(capsule.getUrl());
					image = ImageIO.read(url);
					if (image.getHeight() != 300 || image.getWidth() != 300) {

						image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 300,
								300, Scalr.OP_ANTIALIAS);
					}
					drawImage(image, capsule);
					g.drawImage(image, x * 300, y * 300, null);

					//g.drawImage(image, x * 300, y * 300, x * 300 + 300, y * 300 + 300, 0, 0, image.getWidth(), image.getHeight(), null);


				} catch (Exception e) {
					e.printStackTrace();
					Color temp = g.getColor();
					g.setColor(Color.WHITE);
					int pos = capsule.getPos();

					int y = (int) Math.floor(pos / this.x);
					int x = pos % this.x;
					g.fillRect(x * 300, y * 300, 300, 300);
					g.setColor(Color.BLACK);

					drawNames(capsule, y, x, g, 300);
					g.setColor(temp);
					e.printStackTrace();
				}


			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}

	private void drawImage(BufferedImage image, UrlCapsule capsule) {
		int a = image.getRGB(0, 0);
		Color myColor = new Color(a);
		Graphics2D gTemp = image.createGraphics();
		GraphicUtils.setQuality(gTemp);
		gTemp.setColor(getBetter(myColor));
		drawNames(capsule, 0, 0, gTemp, image.getWidth());
		gTemp.dispose();
	}

	void drawNames(UrlCapsule capsule, int y, int x, Graphics2D g, int imageWidth) {
		String artistName = capsule.getArtistName();
		String albumName = capsule.getAlbumName();
		Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
		Font albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
		g.setFont(artistFont);

		int artistWidth = g.getFontMetrics().stringWidth(artistName);

		g.setFont(albumFont);

		int albumWidth = g.getFontMetrics().stringWidth(albumName);

		while (artistWidth > imageWidth && fontSize1-- > 14) {
			artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize1);
			g.setFont(artistFont);
			artistWidth = g.getFontMetrics().stringWidth(artistName);
		}
		g.setFont(artistFont);
		FontMetrics metric = g.getFontMetrics();
		int accum = metric.getAscent() - metric.getDescent() - metric.getLeading();
		g.drawString(capsule.getArtistName(), x * 300, y * 300 + accum);


		while (albumWidth > imageWidth && fontSize2-- > 14) {
			fontSize2--;
			albumFont = new Font("ROBOTO-REGULAR", Font.PLAIN, fontSize2);
			g.setFont(albumFont);
			albumWidth = g.getFontMetrics().stringWidth(albumName);
		}


		metric = g.getFontMetrics();
		accum += metric.getAscent() - metric.getDescent() - metric.getLeading() + 1;
		g.drawString(capsule.getAlbumName(), x * 300, y * 300 + accum);
		fontSize1 = START_FONT_SIZE;
		fontSize2 = START_FONT_SIZE;

	}

	private Color getBetter(Color color) {
		double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
		return y < 128 ? Color.WHITE : Color.BLACK;

	}

}
