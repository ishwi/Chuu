package main.ImageRenderer;

import java.awt.*;
import java.awt.font.GlyphVector;

public class GraphicUtils {

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
}
