package core.imagerenderer;


import dao.entities.AlbumUserPlays;
import dao.entities.ArtistAlbums;
import dao.entities.ReturnNowPlaying;
import dao.entities.WrapperReturnNowPlaying;
import core.Chuu;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BandRendered {
	private static final int X_MAX = 1500;
	private static final int Y_MAX = 1000;
	private static final int X_MARGIN = 25;
//	private static final int Y_MARGIN = 50;

	//	private static final String PATH_NO_IMAGE = "C:\\Users\\Ishwi\\Pictures\\New folder\\noArtistImage.png";
	private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 32);
//	private static final Font KOREAN_FONT = new Font("Noto Serif CJK KR Regular", Font.PLAIN, 32);

	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP", Font.PLAIN, 32);

//	private static final Font CHINESE_FONT = new Font("Noto Serif CJK TC Regular", Font.PLAIN, 32);


	private static final Font DESC_FONT = new Font("Noto Sans CJK JP Light", Font.PLAIN, 32);
//	private static final String FIRST_LINE = "Who knows";

	public static BufferedImage makeBandImage(WrapperReturnNowPlaying wrapperReturnNowPlaying, ArtistAlbums ai, int plays, BufferedImage logo, String user) {
		BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
		BufferedImage lastFmLogo;
		BufferedImage artistReplacement;

		String artist = wrapperReturnNowPlaying.getArtist();
		boolean needsJapanese = false;
		try (InputStream in = BandRendered.class.getResourceAsStream("/images/logo2.png")) {
			lastFmLogo = ImageIO.read(in);
			lastFmLogo = Scalr.resize(lastFmLogo, 30);
		} catch (IOException e) {
			lastFmLogo = null;
		}
		artistReplacement = GraphicUtils.noArtistImage;

		BufferedImage artistImageFill = GraphicUtils
				.getImageFromUrl(wrapperReturnNowPlaying.getUrl(), artistReplacement);
		Graphics2D g = GraphicUtils.initArtistBackground(canvas, artistImageFill);

		Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
		Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));
		Color FONT_COLOR = GraphicUtils.getBetter(GraphicUtils.getFontColorBackground(canvas));

		g.setColor(FONT_COLOR);

		if (NORMAL_FONT.canDisplayUpTo(artist) != -1) {
			needsJapanese = true;
		}

		List<AlbumUserPlays> albumUserPlaysList = ai.getAlbumList();
		int count = 0;
		List<BufferedImage> albumsImages = new ArrayList<>(4);
		for (AlbumUserPlays albumUserPlays : albumUserPlaysList) {
			if (count++ == 4)
				break;
			try {

				if (NORMAL_FONT.canDisplayUpTo(albumUserPlays.getAlbum()) != -1) {
					needsJapanese = true;
				}

				java.net.URL url = new java.net.URL(albumUserPlays.getAlbum_url());
				albumsImages.add(ImageIO.read(url));
			} catch (IOException e) {
				Chuu.getLogger().warn(e.getMessage(), e);
				albumsImages.add(null);
			}
		}

		if (needsJapanese)
			g.setFont(JAPANESE_FONT);
		else
			g.setFont(NORMAL_FONT);

		FontMetrics metrics = g.getFontMetrics();
		String people = "Top 5 people";
		int width = metrics.stringWidth(people);
		GraphicUtils.drawStringNicely(g, people, (X_MARGIN + 40) + (380 / 2) - width / 2, 700 - metrics
				.getAscent(), canvas);
		//g.drawString(people, (X_MARGIN + 40) + (380 / 2) - width / 2, 700 - metrics.getAscent());
		GraphicUtils
				.doChart(g, X_MARGIN + 40, 700 - 20, 400, 50, 5, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, DESC_FONT
						.deriveFont(36f));

		count = 0;
		int images_drawn = 0;

		int albumsStartPosition = X_MARGIN + 400 + 195 + 40;
		for (BufferedImage albumsImage : albumsImages) {
			count++;
			if (albumsImage == null) {
				continue;
			}
			int pos_x;
			int baseline;
			switch (albumsImages.size()) {
				case 3: {
					int[] pos = {20, 370, 175};
					pos_x = albumsStartPosition + pos[count - 1];
					baseline = 105 + 400 * (images_drawn / 2);
					break;
				}
				case 2: {
					pos_x = albumsStartPosition + 175;
					baseline = 105 + 400 * (images_drawn);
					break;
				}
				case 1: {
					pos_x = albumsStartPosition + 175;
					baseline = 105 + 200;
					break;
				}
				default: {
					pos_x = albumsStartPosition + 350 * (images_drawn % 2);
					baseline = 105 + 400 * (images_drawn / 2);
					break;
				}
			}
			g.drawImage(albumsImage, pos_x, baseline, 300, 300, null);
			baseline += 300;
			AlbumUserPlays albumUserPlays = albumUserPlaysList.get(count - 1);
			String album = albumUserPlays.getAlbum();

			String play = Integer.toString(albumUserPlays.getPlays());

			Font ogFont = g.getFont();
			float sizeFont = ogFont.getSize();
			while ((width = g.getFontMetrics(g.getFont()).stringWidth(album)) > 300 && sizeFont > 8f) {
				g.setFont(g.getFont().deriveFont(sizeFont -= 2));
			}
			GraphicUtils
					.drawStringNicely(g, album, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent(), canvas);

			//;g.drawString(album, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent());
			g.setFont(ogFont);

			baseline += metrics.getAscent() + metrics.getDescent();
			width = metrics.stringWidth(play);
			int start = pos_x + (300 / 2) - width / 2;
			int finish = start + width;
			width += 25;

			GraphicUtils
					.drawStringNicely(g, play, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent(), canvas);
			//g.drawString(play, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent());
			g.drawImage(lastFmLogo, finish, baseline + metrics.getAscent() - metrics.getDescent() - metrics
					.getLeading() - 8, null);
			images_drawn++;
		}

		int yBaseLine = 380;
		if (artistImageFill != null) {
			g.drawImage(Scalr
					.resize(artistImageFill, yBaseLine, Scalr.OP_ANTIALIAS), X_MARGIN + 40 + (400 - 380) / 2, 25, null);
		}
		width = metrics.stringWidth(artist);
		yBaseLine += metrics.getAscent() + metrics.getDescent() + metrics.getLeading() + 20;
		GraphicUtils.drawStringNicely(g, artist, X_MARGIN + 40 + (380 / 2) - width / 2, yBaseLine, canvas);

//				;g.drawString(artist, X_MARGIN + 40 + (380 / 2) - width / 2, yBaseLine);

		ReturnNowPlaying myRow = new ReturnNowPlaying(1, user, artist, plays);
		myRow.setDiscordName(user);

		WrapperReturnNowPlaying wrapper1Row = new WrapperReturnNowPlaying(Collections
				.singletonList(myRow), 1, artist, artist);
		GraphicUtils.doChart(g, X_MARGIN + 40, yBaseLine + metrics
				.getAscent() - 20, 400, 50, 1, wrapper1Row, colorB1, colorB, lastFmLogo, false, DESC_FONT
				.deriveFont(36f));

		return canvas;
	}
}
