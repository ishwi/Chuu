package main.imagerenderer;

import dao.entities.UrlCapsule;
import main.Chuu;
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
	private final AtomicInteger iterations;
	private final Font START_FONT;
	private int START_FONT_SIZE = 24;

	private boolean writePlays = false;
	private boolean writeTitles = true;
	private int lowerLimitStringSize = 14;
	private int imageSize = 300;

	ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations) {
		this.queue = queue;
		this.g = g;
		this.x = x;
		this.y = y;
		this.iterations = iterations;
		START_FONT = new Font("Fira Sans Book", Font.PLAIN, START_FONT_SIZE);

	}

	ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations, boolean writePlays, boolean writeTitles, boolean makeSmaller) {
		this.queue = queue;
		this.g = g;
		this.y = y;
		this.x = x;
		this.iterations = iterations;
		this.writePlays = writePlays;
		this.writeTitles = writeTitles;
		if (makeSmaller) {
			this.imageSize = 150;
			START_FONT_SIZE = 12;
			lowerLimitStringSize = 7;
		}
		START_FONT = new Font("Fira Sans Book", Font.PLAIN, START_FONT_SIZE);
	}


	@Override
	public void run() {
		while (iterations.getAndDecrement() > 0) {

			g.setFont(START_FONT);
			g.setColor(Color.BLACK);

			try {
				UrlCapsule capsule = queue.take();
				BufferedImage image;
				URL url;
				int pos = capsule.getPos();
				int y = (pos / this.x);
				int x = pos % this.x;

				try {

					url = new URL(capsule.getUrl());
					image = ImageIO.read(url);
					if (image.getHeight() != imageSize || image.getWidth() != imageSize) {

						image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, imageSize,
								imageSize, Scalr.OP_ANTIALIAS);
					}
					drawImage(image, capsule);
					g.drawImage(image, x * imageSize, y * imageSize, null);

					//g.drawImage(image, x * imageSize, y * imageSize, x * imageSize + imageSize, y * imageSize + imageSize, 0, 0, image.getWidth(), image.getHeight(), null);

				} catch (Exception e) {
					//Chuu.getLogger().warn(e.getMessage(), e);
					Color temp = g.getColor();
					g.setColor(Color.WHITE);

					g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);
					g.setColor(Color.BLACK);

					drawNames(capsule, y, x, g, imageSize, null);
					g.setColor(temp);
					Chuu.getLogger().warn(e.getMessage(), e);
				}


			} catch (Exception e) {
				Chuu.getLogger().warn(e.getMessage(), e);
			}
		}


	}

	private void drawImage(BufferedImage image, UrlCapsule capsule) {
		int a = image.getRGB(0, 0);
		Color myColor = new Color(a);
		Graphics2D gTemp = image.createGraphics();
		GraphicUtils.setQuality(gTemp);
		gTemp.setColor(getBetter(myColor));
		drawNames(capsule, 0, 0, gTemp, image.getWidth(), image);
		gTemp.dispose();
	}

	void drawNames(UrlCapsule capsule, int y, int x, Graphics2D g, int imageWidth, BufferedImage image) {
		String artistName = capsule.getArtistName();
		String albumName = capsule.getAlbumName();
		String plays = capsule.getPlays() + (capsule.getPlays() > 1 ? " plays" : "play");

		int accum = 3;
		if (this.writeTitles) {
			Font artistFont = START_FONT;
			int artistFontsSize = START_FONT_SIZE;
			int albumFontsSize = START_FONT_SIZE;

			Font albumFont = START_FONT;

			g.setFont(artistFont);

			int artistWidth = g.getFontMetrics().stringWidth(artistName);

			while (artistWidth > imageWidth && artistFontsSize-- > lowerLimitStringSize) {
				artistFont = artistFont.deriveFont((float) artistFontsSize);
				g.setFont(artistFont);
				artistWidth = g.getFontMetrics().stringWidth(artistName);
			}
			FontMetrics metric = g.getFontMetrics();
			accum = metric.getAscent() - metric.getDescent() - metric.getLeading() + 3;
			g.setFont(artistFont);
			if (image != null) {
				g.setColor(Color.BLACK);
				g.drawString(capsule.getArtistName(), 0, accum);
				GraphicUtils.drawStringChartly(g, capsule.getArtistName(), 0, accum, image);
			} else {
				g.setColor(Color.BLACK);
				g.drawString(capsule.getArtistName(), x * imageSize, y * imageSize + accum);
			}

			if (!albumName.isEmpty()) {
				g.setFont(albumFont);

				int albumWidth = g.getFontMetrics().stringWidth(albumName);

				while (albumWidth > imageWidth && albumFontsSize-- > lowerLimitStringSize) {
					albumFontsSize--;
					albumFont = albumFont.deriveFont((float) albumFontsSize);
					g.setFont(albumFont);

					albumWidth = g.getFontMetrics().stringWidth(albumName);
				}

				metric = g.getFontMetrics();
				accum += metric.getAscent() - metric.getDescent() - metric.getLeading() + 3;
				if (image != null) {
					g.setColor(Color.BLACK);
					g.drawString(capsule.getAlbumName(), 0, accum);
					GraphicUtils.drawStringChartly(g, capsule.getAlbumName(), 0, accum, image);
				} else {
					g.setColor(Color.BLACK);
					g.drawString(capsule.getAlbumName(), x * imageSize, y * imageSize + accum);
				}
			}
		}
		if (writePlays) {
			Font playFonts = START_FONT;
			int playFontsSize = START_FONT_SIZE;

			g.setFont(playFonts);

			int playWidth = g.getFontMetrics().stringWidth(plays);

			while (playWidth > imageWidth / 2 && playFontsSize-- > lowerLimitStringSize) {
				playFonts = playFonts.deriveFont((float) playFontsSize);
				g.setFont(playFonts);
				playWidth = g.getFontMetrics().stringWidth(plays);
			}
			FontMetrics metric = g.getFontMetrics();
			accum += metric.getAscent() - metric.getDescent() - metric.getLeading() + 3;
			if (image != null) {
				GraphicUtils.drawStringChartly(g, plays, 0, accum, image);

			} else {
				g.setColor(Color.BLACK);
				g.drawString(plays, x * 300, y * 300 + accum);
			}
		}
	}

	private Color getBetter(Color color) {
		double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
		return y < 128 ? Color.WHITE : Color.BLACK;

	}

}
