package core.imagerenderer;

import dao.entities.WrapperReturnNowPlaying;
import core.imagerenderer.stealing.GaussianFilter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WhoKnowsMaker {

	static final Font EMOJI_FONT = new Font("Symbola", Font.PLAIN, 32);
	private static final int X_MAX = 800;
	private static final int Y_MAX = 500;
	private static final int X_MARGIN = 80;
	private static final int Y_MARGIN = 25;
	//private static final Font KOREAN_FONT = new Font("Noto Serif CJK KR Regular", Font.PLAIN, 32);
	private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 32);
	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP", Font.PLAIN, 32);

	//private static final Font CHINESE_FONT = new Font("Noto Serif CJK TC Regular", Font.PLAIN, 32);
	private static final Font DESC_FONT = new Font("Noto Sans CJK JP Light", Font.PLAIN, 32);
	private static final String FIRST_LINE = "Who knows";
	//private static Color FONT_COLOR = Color.BLACK;

	public static BufferedImage generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, String discordName, BufferedImage logo) {

		BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
		String artist = wrapperReturnNowPlaying.getArtist().toUpperCase();
		String urlString = wrapperReturnNowPlaying.getUrl();
		FontMetrics metrics;

		int width;
		int y_counter = 0;
		y_counter += Y_MARGIN;
		BufferedImage backgroundImage;
		BufferedImage lastFmLogo = null;
		BufferedImage guildLogo = null;

		Graphics2D g = canvas.createGraphics();
		GraphicUtils.setQuality(g);

		try {

			lastFmLogo = ImageIO.read(WhoKnowsMaker.class
					.getResourceAsStream("/images/logo2.png"));//ImageIO.read(new File("C:\\Users\\Ishwi\\Documents\\discord\\bot\\src\\main\\resources\\logo2.png"));
			guildLogo = logo;//new File("C:\\Users\\Ishwi\\Desktop\\logo.png"));

			java.net.URL url = new java.net.URL(urlString);
			backgroundImage = ImageIO.read(url);

		} catch (IOException e) {
			backgroundImage = GraphicUtils.noArtistImage;
		}

		g.drawImage(backgroundImage, 0, 0, X_MAX, Y_MAX, 0, 0, backgroundImage.getWidth(), backgroundImage
				.getHeight(), null);
		new GaussianFilter(90).filter(canvas, canvas);

		Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
		Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));
		//FONT_COLOR = (GraphicUtils.getBetter(new Color(canvas.getRGB(X_MAX / 2, Y_MAX / 2))));

		//g.setColor(FONT_COLOR);

		g.setFont(DESC_FONT);
		metrics = g.getFontMetrics(DESC_FONT);
		width = metrics.stringWidth("Who knows");
		y_counter += metrics.getAscent() - metrics.getDescent();

		GraphicUtils.drawStringNicely(g, FIRST_LINE, X_MAX / 2 - width / 2, y_counter, canvas);

		y_counter += 10;
		Font fontToUse;
		if (NORMAL_FONT.canDisplayUpTo(artist) == -1) {
			fontToUse = NORMAL_FONT;

		} else
			fontToUse = JAPANESE_FONT;

		g.setFont(fontToUse);
		metrics = g.getFontMetrics(fontToUse);
		y_counter += metrics.getAscent() - metrics.getDescent();
		float size = 32;
		while ((width = g.getFontMetrics(g.getFont()).stringWidth(artist)) > (canvas.getWidth() * 0.70) && size > 14f) {
			g.setFont(g.getFont().deriveFont(size -= 2));
		}
		//GraphicUtils.do1(g,artist,GraphicUtils.getInverseBW(FONT_COLOR),FONT_COLOR,X_MAX / 2 - width / 2,y_counter);
		GraphicUtils.drawStringNicely(g, artist, X_MAX / 2 - width / 2, y_counter, canvas);

		y_counter += metrics.getDescent();

		g.setFont(DESC_FONT);
		metrics = g.getFontMetrics(DESC_FONT);
		y_counter += metrics.getAscent() - metrics.getDescent();
		String thirdLine = "in " + discordName;
		width = metrics.stringWidth(thirdLine);
		GraphicUtils.drawStringNicely(g, thirdLine, X_MAX / 2 - width / 2, y_counter, canvas);
		y_counter += 16;

		int rectWidth = X_MAX - X_MARGIN - (X_MARGIN + 320);

		backgroundImage = Scalr.resize(backgroundImage, Scalr.Method.QUALITY, 320, Scalr.OP_ANTIALIAS);
		int x_image_starter = X_MARGIN + (320 - backgroundImage.getWidth()) / 2;
		int y_image_starter = y_counter + (320 - backgroundImage.getHeight()) / 2;
		g.drawImage(backgroundImage, x_image_starter, y_image_starter, null);
		if (guildLogo != null)
			g.drawImage(guildLogo, X_MARGIN + 320 + rectWidth - guildLogo.getWidth(), y_counter - 16 - guildLogo
					.getHeight(), null);

		GraphicUtils
				.doChart(g, X_MARGIN + 320, y_counter, rectWidth, 32, 10, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, DESC_FONT
						.deriveFont(18f));
		return canvas;
	}


}
