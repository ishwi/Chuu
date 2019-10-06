package main.imagerenderer;

import dao.entities.ProfileEntity;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ProfileMaker {
	private static final int X_SIZE = 1920;
	private static final int Y_SIZE = 1080;
	private static final int TEXT_END = 450 + 45;

	private static final int IMAGE_START = X_SIZE - TEXT_END + 55;
	private static final int ARTIST_IMAGES_SIZE = 300;
	private static final int CROWN_IMAGE_Y = 250;
	private static final int UNIQUE_IMAGE_Y = 250;
	private static final int AVATAR_SIZE = 250;

	private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 44);

	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP Regular", Font.PLAIN, 44);


	private static final int RIGHT_MARGIN = 55;


	public static BufferedImage makeProfile(ProfileEntity entity) {
		try {

			BufferedImage image = new BufferedImage(X_SIZE, Y_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			GraphicUtils.setQuality(g);
			GraphicUtils.initRandomImageBlurredBackground(g, X_SIZE, Y_SIZE);
			BufferedImage imageToDraw;

			Font fonttoUse = NORMAL_FONT;
			if (NORMAL_FONT.canDisplayUpTo(entity.getUniqueArtist()) != -1 || NORMAL_FONT
					.canDisplayUpTo(entity.getCrownArtist()) != -1) {
				fonttoUse = JAPANESE_FONT;
			}
			g.setFont(fonttoUse);

			final Font ogFont = g.getFont();

			//CrownImage
			imageToDraw = GraphicUtils.getImageFromUrl(entity.getCrownUrl(), GraphicUtils.noArtistImage);
			int baseline = 115;
			makeDrawingStringProcess("Top Crown", g, image, ogFont, IMAGE_START - 25, 350, baseline);
			g.drawImage(Scalr.resize(imageToDraw, ARTIST_IMAGES_SIZE), IMAGE_START, 175, null);
			baseline += 355;
			makeDrawingStringProcess(entity
					.getCrownArtist(), g, image, ogFont, IMAGE_START - 25, 350, baseline);
			baseline += 95;

			//UniqueImage
			imageToDraw = GraphicUtils.getImageFromUrl(entity.getUniqueUrl(), GraphicUtils.noArtistImage);
			makeDrawingStringProcess("Top Unique", g, image, ogFont, IMAGE_START - 25, 350, baseline);
			baseline += 55;
			g.drawImage(Scalr
					.resize(imageToDraw, ARTIST_IMAGES_SIZE), IMAGE_START, baseline, null);
			baseline += 300;
			makeDrawingStringProcess(entity.getUniqueArtist(), g, image, ogFont, IMAGE_START - 25, 350, baseline);

			//AvatarImage
			imageToDraw = GraphicUtils.getImageFromUrl(entity.getLastfmUrl(), null);
			if (imageToDraw == null) {
				imageToDraw = GraphicUtils.getImageFromUrl(entity.getDiscordUrl(), GraphicUtils.noArtistImage);
			}

			g.drawImage(Scalr.resize(imageToDraw, AVATAR_SIZE), (X_SIZE - AVATAR_SIZE) / 2 - 350, 50, null);

			g.setFont(fonttoUse.deriveFont(64f));

			GraphicUtils.drawStringNicely(g, entity
					.getUsername(), (X_SIZE - AVATAR_SIZE) / 2 - 350 + AVATAR_SIZE + 20, 50 + ((AVATAR_SIZE + g
					.getFontMetrics().getAscent()) / 2), image);

			String s;
			int width;
			int increment = (int) ((double) g.getFontMetrics().getMaxAscent() * 1.5);
			baseline = 425;

			GraphicUtils.drawStringNicely(g, "Total Number of scrobbles", 25, baseline, image);
			s = String.valueOf(entity.getScrobbles());
			width = g.getFontMetrics(g.getFont()).stringWidth(s);
			GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
			baseline += increment;

			GraphicUtils.drawStringNicely(g, "Total Number of albums", 25, baseline, image);
			s = String.valueOf(entity.getAlbums());
			width = g.getFontMetrics(g.getFont()).stringWidth(s);
			GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
			baseline += increment;

			GraphicUtils.drawStringNicely(g, "Total Number of artists", 25, baseline, image);
			s = String.valueOf(entity.getArtist());
			width = g.getFontMetrics(g.getFont()).stringWidth(s);
			GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
			baseline += increment;

			GraphicUtils.drawStringNicely(g, "Total Number of crowns", 25, baseline, image);
			s = String.valueOf(entity.getCrowns());
			width = g.getFontMetrics(g.getFont()).stringWidth(s);
			GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
			baseline += increment;

			GraphicUtils.drawStringNicely(g, "Total Number of unique artists", 25, baseline, image);
			s = String.valueOf(entity.getUniques());
			width = g.getFontMetrics(g.getFont()).stringWidth(s);

			GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);
			baseline += increment;

			GraphicUtils.drawStringNicely(g, "Obscurity Score", 25, baseline, image);
			s = String.valueOf(entity.getObscurityScore());
			width = g.getFontMetrics(g.getFont()).stringWidth(s);
			GraphicUtils.drawStringNicely(g, s, 1300 - width, baseline, image);

			g.dispose();
			return image;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	private static void makeDrawingStringProcess(String string, Graphics2D g, BufferedImage image, Font ogFont, int X_STARTING_POINT, int widthFit, int Y_STARTING_POINT) {
		int width = GraphicUtils.fitString(string, g, widthFit, 14f);
		FontMetrics fontMetrics = g.getFontMetrics();
		GraphicUtils
				.drawStringNicely(g, string, X_STARTING_POINT + (widthFit / 2) - width / 2, Y_STARTING_POINT + fontMetrics
						.getAscent(), image);
		g.setFont(ogFont);
	}
}
