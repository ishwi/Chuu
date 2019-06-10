package main.ImageRenderer;

import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.Stealing.GaussianFilter;
import org.imgscalr.Scalr;

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
	private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 32);
	private static final Font KOREAN_FONT = new Font("Noto Serif CJK KR Regular", Font.PLAIN, 32);

	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP Regular", Font.PLAIN, 32);

	private static final Font CHINESE_FONT = new Font("Noto Serif CJK TC Regular", Font.PLAIN, 32);


	private static final Font DESC_FONT = new Font("Noto Sans Display Light", Font.PLAIN, 32);
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

			lastFmLogo = ImageIO.read(NPMaker.class.getResourceAsStream("/logo2.png"));//ImageIO.read(new File("C:\\Users\\Ishwi\\Documents\\discord\\bot\\src\\main\\resources\\logo2.png"));
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


		Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
		Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));
		FONT_COLOR = GraphicUtils.getBetter(GraphicUtils.getFontColorBackground(canvas));

		g.setColor(FONT_COLOR);

		g.setFont(DESC_FONT);
		metrics = g.getFontMetrics(DESC_FONT);
		width = metrics.stringWidth("Who knows");
		y_counter += metrics.getAscent() - metrics.getDescent();

		g.drawString(FIRST_LINE, X_MAX / 2 - width / 2, y_counter);

		y_counter += 10;
		Font fontToUse;
		if (NORMAL_FONT.canDisplayUpTo(artist) == -1) {
			fontToUse = NORMAL_FONT;

//		} else if (GraphicUtils.hasKorean(artist)) {
//			fontToUse = KOREAN_FONT;
//
//		} else if (GraphicUtils.hasJapanese(artist)) {
//			fontToUse = JAPANESE_FONT;
//
		} else
			fontToUse = JAPANESE_FONT;

		g.setFont(fontToUse);
		metrics = g.getFontMetrics(fontToUse);
		y_counter += metrics.getAscent() - metrics.getDescent();

		width = metrics.stringWidth(artist);
		//GraphicUtils.do1(g,artist,GraphicUtils.getInverseBW(FONT_COLOR),FONT_COLOR,X_MAX / 2 - width / 2,y_counter);
		g.drawString(artist, X_MAX / 2 - width / 2, y_counter);

		y_counter += 10;


		g.setFont(DESC_FONT);
		metrics = g.getFontMetrics(DESC_FONT);
		y_counter += metrics.getAscent() - metrics.getDescent();
		String thirdLine = "in " + discordName;
		width = metrics.stringWidth(thirdLine);
		g.drawString(thirdLine, X_MAX / 2 - width / 2, y_counter);
		y_counter += 16;


		int rectWidth = X_MAX - X_MARGIN - (X_MARGIN + 320);

		backGroundimage = Scalr.resize(backGroundimage, Scalr.Method.QUALITY, 320, Scalr.OP_ANTIALIAS);
		int x_image_starter = X_MARGIN + (320 - backGroundimage.getWidth()) / 2;
		int y_image_starter = y_counter + (320 - backGroundimage.getHeight()) / 2;
		g.drawImage(backGroundimage, x_image_starter, y_image_starter, null);
		if (guildLogo != null)
			g.drawImage(guildLogo, X_MARGIN + 320 + rectWidth - guildLogo.getWidth(), y_counter - 16 - guildLogo.getHeight(), null);

		doChart(g, X_MARGIN + 320, y_counter, rectWidth, 320, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo);
		return canvas;
	}

	public static void doChart(Graphics2D g, int x, int y_counter, int widht, int height, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage lastFmLogo) {
		doChart(g, x, y_counter, widht, height, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, true);
	}

	public static void doChart(Graphics2D g, int x, int y_counter, int widht, int height, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage lastFmLogo, boolean doNumber) {

		Font ogFont = g.getFont();
		g.setColor(colorB1.brighter());
		g.fillRect(x, y_counter, widht, height);
		g.setColor(colorB);

		g.fillRect(x, y_counter, widht, height);
		FontMetrics metrics;
		g.setFont(DESC_FONT.deriveFont(18f));
		metrics = g.getFontMetrics(g.getFont());
		List<ReturnNowPlaying> nowPlayingArtistList = wrapperReturnNowPlaying.getReturnNowPlayings();
		for (int i = 0; i < nowPlayingArtistList.size() && i < 10; i++) {
			g.setColor(colorB1);

			g.fillRect(x, y_counter, widht, 28);

			g.setColor(GraphicUtils.getBetter(colorB1));

			float size = 18f;
			while (g.getFontMetrics(g.getFont()).stringWidth(nowPlayingArtistList.get(i).getDiscordName()) > widht - 20 && size > 14) {
				g.setFont(g.getFont().deriveFont(size -= 1));
			}
			size = 18f;
			String name = nowPlayingArtistList.get(i).getDiscordName();
			String stringWrite = doNumber ? "#" + (i + 1) + " " + name : " " + name;
			g.drawString(stringWrite, x, y_counter + (30 - metrics.getAscent() / 2));
			g.setFont(DESC_FONT.deriveFont(size));
			String plays = String.valueOf(nowPlayingArtistList.get(i).getPlaynumber());

			g.drawString(plays, x + widht - (34 + metrics.stringWidth(plays)), y_counter + (30 - metrics.getAscent() / 2));
			g.drawImage(lastFmLogo, x + widht - 28, y_counter + 9, null);
			y_counter += 32;

		}
		g.setFont(ogFont);
	}


}
