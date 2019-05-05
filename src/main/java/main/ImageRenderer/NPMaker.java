package main.ImageRenderer;

import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.Stealing.GaussianFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class NPMaker {

	private static final int X_MAX = 800;
	private static final int Y_MAX = 500;
	private static final int X_MARGIN = 80;
	private static final int Y_MARGIN = 25;

	private static final String PATH_NO_IMAGE = "C:\\Users\\Ishwi\\Pictures\\New folder\\818148bf682d429dc215c1705eb27b98.png";
	private static final Font ARTIST_FONT = new Font("Yu Gothic Bold", Font.PLAIN, 32);
	private static final Font DESC_FONT = new Font("Yu Gothic UI Light", Font.PLAIN, 32);
	private static final String FIRST_LINE = "Who knows";
	private static Color FONT_COLOR = Color.BLACK;

	public static BufferedImage generateTasteImage(WrapperReturnNowPlaying wrapperReturnNowPlaying, String discordName, BufferedImage logo) {


		BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
		String artist = wrapperReturnNowPlaying.getArtist().toUpperCase();
		String urlString = wrapperReturnNowPlaying.getUrl();
		FontMetrics metrics;


		int width;
		int y_counter = 0;
		y_counter += Y_MARGIN;
		BufferedImage backGroundimage;
		BufferedImage lastFmLogo = null;
		BufferedImage guildLogo = null;

		Graphics2D g = canvas.createGraphics();
		GraphicUtils.setQuality(g);

		try {
			lastFmLogo = ImageIO.read(new File("C:\\Users\\Ishwi\\Documents\\discord\\bot\\src\\main\\resources\\logo2.png"));
			guildLogo = logo;//new File("C:\\Users\\Ishwi\\Desktop\\logo.png"));


			java.net.URL url = new java.net.URL(urlString);
			backGroundimage = ImageIO.read(url);

		} catch (IOException e) {
			try {
				backGroundimage = ImageIO.read(new File(PATH_NO_IMAGE));
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		}

		g.drawImage(backGroundimage, 0, 0, X_MAX, Y_MAX, 0, 0, backGroundimage.getWidth(), backGroundimage.getHeight(), null);
		new GaussianFilter(90).filter(canvas, canvas);
		float[] rgb2 = new float[3];
		int a = canvas.getRGB(0, 0);
		new Color(a).getRGBColorComponents(rgb2);
		Color colorB = new Color(rgb2[0], rgb2[1], rgb2[2], 0.5f).darker();
		Color colorB1 = new Color(rgb2[0], rgb2[1], rgb2[2], 0.7f);
		colorB1 = colorB1.darker().darker();
		FONT_COLOR = getBetter(new Color(a));
		g.setColor(FONT_COLOR);

		g.setFont(DESC_FONT);
		metrics = g.getFontMetrics(DESC_FONT);
		width = metrics.stringWidth("Who knows");
		y_counter += metrics.getAscent() - metrics.getDescent();

		g.drawString(FIRST_LINE, X_MAX / 2 - width / 2, y_counter);

		y_counter += 8;


		g.setFont(ARTIST_FONT);
		metrics = g.getFontMetrics(ARTIST_FONT);
		y_counter += metrics.getAscent() - metrics.getDescent();

		width = metrics.stringWidth(artist);
		g.drawString(artist, X_MAX / 2 - width / 2, y_counter);

		y_counter += 12.5;


		g.setFont(DESC_FONT);
		metrics = g.getFontMetrics(DESC_FONT);
		y_counter += metrics.getAscent() - metrics.getDescent();
		String thirdLine = "in " + discordName;
		width = metrics.stringWidth(thirdLine);
		g.drawString(thirdLine, X_MAX / 2 - width / 2, y_counter);
		y_counter += 16;


		g.drawImage(backGroundimage, X_MARGIN, y_counter, X_MARGIN + 320, y_counter + 320, 0, 0, backGroundimage.getWidth(), backGroundimage.getHeight(), null);


		g.setColor(colorB);

		int rectWidth = X_MAX - X_MARGIN - (X_MARGIN + 320);
		if (guildLogo != null)
			g.drawImage(guildLogo, X_MARGIN + 320 + rectWidth - guildLogo.getWidth(), y_counter - 16 - guildLogo.getHeight(), null);

		g.fillRect(X_MARGIN + 320, y_counter, rectWidth, 320);

		g.setFont(DESC_FONT.deriveFont(18f));
		metrics = g.getFontMetrics(g.getFont());
		List<ReturnNowPlaying> nowPlayingArtistList = wrapperReturnNowPlaying.getReturnNowPlayings();
		for (int i = 0; i < nowPlayingArtistList.size() && i < 10; i++) {
			g.setColor(colorB1);

			g.fillRect(X_MARGIN + 320, y_counter, rectWidth, 28);

			g.setColor(getBetter(colorB1));

			float size = 18f;
			while (g.getFontMetrics(g.getFont()).stringWidth(nowPlayingArtistList.get(i).getDiscordName()) > 190 && size > 14) {
				g.setFont(g.getFont().deriveFont(size -= 1));
			}
			size = 18f;


			g.drawString("#" + (i + 1) + " " + nowPlayingArtistList.get(i).getDiscordName(), X_MARGIN + 332, y_counter + (32 - metrics.getAscent() / 2));
			g.setFont(DESC_FONT.deriveFont(size));
			String plays = String.valueOf(nowPlayingArtistList.get(i).getPlaynumber());

			g.drawString(plays, X_MARGIN + 320 + rectWidth - (34 + metrics.stringWidth(plays)), y_counter + (30 - metrics.getAscent() / 2));
			g.drawImage(lastFmLogo, X_MARGIN + 320 + rectWidth - 28, y_counter + 9, null);
			y_counter += 32;

		}

		return canvas;
	}

	private static Color getBetter(Color color) {
		double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
		return y < 128 ? Color.WHITE : Color.BLACK;

	}
}
