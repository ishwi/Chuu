package main.ImageRenderer;

import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.Stealing.GaussianFilter;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

class GraphicUtils {
	private static final String PATH_NO_IMAGE = "C:\\Users\\Ishwi\\Pictures\\New folder\\noArtistImage.png";

	public static int getStringAscent(Graphics page, Font f, String s) {
		// Find the size of string s in the font of the Graphics context "page"
		FontMetrics fm = page.getFontMetrics(f);
		return fm.getAscent();
	}

	public static Color getInverseBW(Color color) {
		return color.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
	}


	public static Graphics2D initArtistBackground(BufferedImage canvas, BufferedImage artistImage) {

		Graphics2D g = canvas.createGraphics();
		GraphicUtils.setQuality(g);
		if (artistImage == null) {
			return g;
		}
		g.drawImage(artistImage, 0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0, artistImage.getWidth(), artistImage
				.getHeight(), null);
		new GaussianFilter(90).filter(canvas, canvas);
		return g;
	}

	public static void setQuality(Graphics2D g) {
		g.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
	}

	public static Color getFontColorBackground(BufferedImage canvas) {
		int a = canvas.getRGB(0, 0);
		return new Color(a);
	}

	public static Color MakeMoreTransParent(Color fontColor, float percentage) {
		float[] rgb2 = new float[3];
		fontColor.getRGBColorComponents(rgb2);
		return new Color(rgb2[0], rgb2[1], rgb2[2], percentage);

	}

	public static Color getReadableColorBackgroundForFont(Color fontColor) {
		float[] rgb2 = new float[3];
		fontColor.getRGBColorComponents(rgb2);
		Color colorB1 = new Color(rgb2[0], rgb2[1], rgb2[2], 0.7f);
		return colorB1.darker().darker();
	}

	public static Color getSurfaceColor(Color fontColor) {
		float[] rgb2 = new float[3];
		fontColor.getRGBColorComponents(rgb2);
		return new Color(rgb2[0], rgb2[1], rgb2[2], 0.5f).darker();
	}

	public static BufferedImage getImageFromUrl(String urlImage, @Nullable BufferedImage replacement) {
		BufferedImage backgroundImage;
		try {

			java.net.URL url = new java.net.URL(urlImage);
			backgroundImage = ImageIO.read(url);

		} catch (IOException e) {

			backgroundImage = replacement;
		}
		return backgroundImage;

	}

	public static void doChart(Graphics2D g, int x, int y_counter, int width, int height, int max_rows, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage lastFmLogo, Font font) {
		doChart(g, x, y_counter, width, height, max_rows, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, true, font);
	}

	public static void doChart(Graphics2D g, int x, int y_counter, int width, int row_height, int max_rows, WrapperReturnNowPlaying wrapperReturnNowPlaying, Color colorB1, Color colorB, BufferedImage lastFmLogo, boolean doNumber, Font font) {

		Font ogFont = g.getFont();
		Color ogColor = g.getColor();
		g.setColor(colorB1.brighter());
		g.fillRect(x, y_counter, width, row_height * max_rows);
		g.setColor(colorB);

		int row_content = (int) (row_height * 0.9);
		int margin = (int) (row_height * 0.1);

		g.fillRect(x, y_counter, width, row_height * max_rows);
		FontMetrics metrics;
		g.setFont(font);
		float initial_size = g.getFont().getSize();
		metrics = g.getFontMetrics(g.getFont());
		List<ReturnNowPlaying> nowPlayingArtistList = wrapperReturnNowPlaying.getReturnNowPlayings();
		y_counter += metrics.getAscent() + metrics.getDescent();
		for (int i = 0; i < nowPlayingArtistList.size() && i < 10; i++) {
			g.setColor(colorB1);

			g.fillRect(x, y_counter - metrics.getAscent() - metrics.getDescent(), width, row_content);

			g.setColor(GraphicUtils.getBetter(colorB1));

			float size = initial_size;
			String name = nowPlayingArtistList.get(i).getDiscordName();
			Font tempFont = g.getFont();

			int start_name = x;
			if (doNumber) {
				String strNumber = "#" + (i + 1) + " ";
				g.drawString(strNumber, x, y_counter + (margin - metrics.getAscent() / 2));
				start_name += g.getFontMetrics().stringWidth(strNumber);
			}

			if (g.getFont().canDisplayUpTo(name) != -1 && WhoKnowsMaker.EMOJI_FONT.canDisplayUpTo(name) == -1)
				g.setFont(WhoKnowsMaker.EMOJI_FONT.deriveFont(size));

			while (g.getFontMetrics(g.getFont()).stringWidth(name) > (width * 0.55) && size > 14f)
				g.setFont(g.getFont().deriveFont(size -= 2));

			g.drawString(name, start_name, y_counter + (margin - metrics.getAscent() / 2));

			size = initial_size;
			g.setFont(tempFont.deriveFont(size));
			String plays = String.valueOf(nowPlayingArtistList.get(i).getPlayNumber());
			int stringWidth = metrics.stringWidth(plays);
			int playPos = x + width - (row_height + stringWidth);
			int playEnd = playPos + stringWidth;
			g.drawString(plays, x + width - (row_height + metrics.stringWidth(plays)), y_counter + (margin - metrics
					.getAscent() / 2));
			g.drawImage(lastFmLogo, playEnd + 9, (int) (y_counter - metrics.getAscent() * 0.85), null);
			y_counter += row_height;


		}
		g.setFont(ogFont);
		g.setColor(ogColor);
	}

	public static Color getBetter(Color... color) {
		double accum = 0;
		for (Color col : color) {
			accum += 0.2126 * col.getRed() + 0.7152 * col.getGreen() + 0.0722 * col.getBlue();
		}
		return (accum / color.length) < 128 ? Color.WHITE : Color.BLACK;

	}

	public static void drawStringNicely(Graphics2D g, String string, int x, int y, BufferedImage bufferedImage) {
		try {
			Color temp = g.getColor();
			int length = g.getFontMetrics().stringWidth(string);
			Color col1 = new Color(bufferedImage.getRGB(
					Math.min(bufferedImage.getWidth() - 1, Math.max(0, x))
					, y));
			Color col2 = new Color(bufferedImage.getRGB(Math.min(bufferedImage.getWidth() - 1, x + length / 2), y));
			Color col3 = new Color(bufferedImage.getRGB(Math.min(bufferedImage.getWidth() - 1, x + length), y));
//		g.setColor(Color.WHITE);
			g.setColor(getBetter(col1, col2, col3));
			g.drawString(string, x, y);
			g.setColor(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void drawStringChartly(Graphics2D g, String string, int x, int y, BufferedImage bufferedImage) {
		Color temp = g.getColor();
		int length = g.getFontMetrics().stringWidth(string);
		Color col1 = new Color(bufferedImage.getRGB(Math.max(0, x), y));

		Color col2 = new Color(bufferedImage.getRGB(Math.min(bufferedImage.getWidth() - 1, x + length / 2), y));
		Color col3 = new Color(bufferedImage.getRGB(Math.min(bufferedImage.getWidth() - 1, x + length), y));
		g.setColor(Color.WHITE);
		Color better = getBetter(col1, col2, col3);
		Color notBetter = better == Color.WHITE ? Color.BLACK : Color.WHITE;
		g.setColor(notBetter);
		g.drawString(string, x, y);
		g.setColor(getBetter(col1, col2, col3));
		g.drawString(string, x + 1, y);
		g.setColor(temp);
	}

	public static BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		Graphics g = b.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return b;
	}
}

