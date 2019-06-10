package main.ImageRenderer;

import main.ImageRenderer.Stealing.GaussianFilter;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class GraphicUtils {
	private static final String PATH_NO_IMAGE = "C:\\Users\\Ishwi\\Pictures\\New folder\\818148bf682d429dc215c1705eb27b98.png";

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

	public static int getStringAscent(Graphics page, Font f, String s) {
		// Find the size of string s in the font of the Graphics context "page"
		FontMetrics fm = page.getFontMetrics(f);
		return fm.getAscent();
	}

	public static void do1(Graphics2D g2, String text, Color outlineColor, Color inline, int x, int y) {
		g2.translate(x, y);
		Color fillColor = inline;
		BasicStroke outlineStroke = new BasicStroke(1.0f);
		Color originalColor = g2.getColor();
		Stroke originalStroke = g2.getStroke();
		RenderingHints originalHints = g2.getRenderingHints();


		// create a glyph vector from your text
		GlyphVector glyphVector = g2.getFont().createGlyphVector(g2.getFontRenderContext(), text);
		// get the shape object
		Shape textShape = glyphVector.getOutline();
		g2.setColor(outlineColor);
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		//g2.setStroke(outlineStroke);
		g2.draw(textShape); // draw outline

		g2.setColor(fillColor);
		g2.fill(textShape); // fill the shape

		// reset to original settings after painting
		g2.setColor(originalColor);
		g2.setStroke(originalStroke);
		g2.setRenderingHints(originalHints);
		g2.translate(-x, -y);


	}

	public static Color getInverseBW(Color color) {
		return color.equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
	}


	public static boolean hasKorean(CharSequence charSequence) {
		boolean hasKorean = false;
		for (char c : charSequence.toString().toCharArray()) {
			if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_JAMO
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_SYLLABLES) {
				hasKorean = true;
				break;
			}
		}

		return hasKorean;
	}

	public static boolean hasJapanese(CharSequence charSequence) {
		boolean hasJapanese = false;
		for (char c : charSequence.toString().toCharArray()) {
			if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
					|| Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) {
				hasJapanese = true;
				break;
			}
		}

		return hasJapanese;
	}

	private boolean hasChinese(final char c) {
		if ((Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
				|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS)) {
			return true;
		}
		return false;
	}

	public static Graphics2D initArtistBackground(BufferedImage canvas, BufferedImage artistImage) {

		Graphics2D g = canvas.createGraphics();
		GraphicUtils.setQuality(g);

		g.drawImage(artistImage, 0, 0, canvas.getWidth(), canvas.getHeight(), 0, 0, artistImage.getWidth(), artistImage.getHeight(), null);
		new GaussianFilter(90).filter(canvas, canvas);
		return g;
	}

	public static Color getFontColorBackground(BufferedImage canvas) {
		int a = canvas.getRGB(0, 0);
		return new Color(a);
	}

	public static Color getBetter(Color color) {
		double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
		return y < 128 ? Color.WHITE : Color.BLACK;

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

	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static BufferedImage getImageFromUrl(String urlImage, @Nullable BufferedImage replacement) {
		BufferedImage backGroundimage;
		try {


			java.net.URL url = new java.net.URL(urlImage);
			backGroundimage = ImageIO.read(url);

		} catch (IOException e) {

			backGroundimage = replacement;
		}
		return backGroundimage;

	}
}
