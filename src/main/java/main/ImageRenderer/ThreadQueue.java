package main.ImageRenderer;

import DAO.Entities.UrlCapsule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

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

	public ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y) {
		this.queue = queue;
		this.g = g;
		this.x = x;
		this.y = y;

	}

	@Override
	public void run() {
		while (!queue.isEmpty()) {
			g.setFont(new Font("Times New Roman", Font.BOLD, 24));
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
					g.drawImage(image, x * 300, y * 300, null);
					int a = image.getRGB(0, 0);
					Color mycolor = new Color(a);
					g.setColor(getBetter(mycolor));
					int accu = fontHeight - 10;
					drawNames(encapsuler, y, x, accu);
					System.out.println(x + "                " + y);
				} catch (IOException e) {
					Color temp  = g.getColor();
					g.setColor(Color.WHITE);
					int pos = encapsuler.getPos();
					int y = (int) Math.floor(pos / this.x);
					int x = pos % this.x;
					int accu = fontHeight - 10;
					g.fillRect(x*300,y*300,300,300);
					g.setColor(Color.BLACK);

					drawNames(encapsuler, y, x, accu);
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

	private void drawNames(UrlCapsule encapsuler, int y, int x, int accu) {
		g.drawString(encapsuler.getArtistName(), x * 300, y * 300 + accu);
		accu += accu;
		g.drawString(encapsuler.getAlbumName(), x * 300, y * 300 + accu);
	}

	private Color getBetter(Color color) {
		Double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
		return y < 128 ? Color.WHITE : Color.BLACK;

	}
}
