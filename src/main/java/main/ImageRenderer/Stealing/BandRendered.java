package main.ImageRenderer.Stealing;


import DAO.Entities.AlbumInfo;
import DAO.Entities.ArtistAlbums;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.GraphicUtils;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BandRendered {
	private static final int X_MAX = 1500;
	private static final int Y_MAX = 1000;
	private static final int X_MARGIN = 25;
	private static final int Y_MARGIN = 50;

	private static final String PATH_NO_IMAGE = "C:\\Users\\Ishwi\\Pictures\\New folder\\818148bf682d429dc215c1705eb27b98.png";
	private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, 32);
	private static final Font KOREAN_FONT = new Font("Noto Serif CJK KR Regular", Font.PLAIN, 32);

	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP Regular", Font.PLAIN, 32);

	private static final Font CHINESE_FONT = new Font("Noto Serif CJK TC Regular", Font.PLAIN, 32);


	private static final Font DESC_FONT = new Font("Noto Sans Display Light", Font.PLAIN, 32);
	private static final String FIRST_LINE = "Who knows";
	private static Color FONT_COLOR = Color.BLACK;

	public static BufferedImage makeBandImage(WrapperReturnNowPlaying wrapperReturnNowPlaying, ArtistAlbums ai, int plays, BufferedImage logo, String user) {
		BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);
		BufferedImage lastFmLogo = null;
		String artist = wrapperReturnNowPlaying.getArtist();
		boolean needsJapanese = false;
		try {
			lastFmLogo = ImageIO.read(BandRendered.class.getResourceAsStream("/logo2.png"));//ImageIO.read(new File("C:\\Users\\Ishwi\\Documents\\discord\\bot\\src\\main\\resources\\logo2.png"));
			lastFmLogo = Scalr.resize(lastFmLogo, 30);
		} catch (IOException e) {
			return null;
		}
		BufferedImage artistImageFill = GraphicUtils.getImageFromUrl(wrapperReturnNowPlaying.getUrl(), null);
		Graphics2D g = GraphicUtils.initArtistBackground(canvas, artistImageFill);

		Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
		Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));
		FONT_COLOR = GraphicUtils.getBetter(GraphicUtils.getFontColorBackground(canvas));

		g.setColor(FONT_COLOR);

		if (NORMAL_FONT.canDisplayUpTo(artist) != -1) {
			needsJapanese = true;
		}

		List<AlbumInfo> albumInfoList = ai.getAlbumList();
		int count = 0;
		List<BufferedImage> albumsImages = new ArrayList<>(4);
		for (AlbumInfo albumInfo : albumInfoList) {
			if (count++ == 4)
				break;
			try {

				if (NORMAL_FONT.canDisplayUpTo(albumInfo.getAlbum()) != -1) {
					needsJapanese = true;
				}

				java.net.URL url = new java.net.URL(albumInfo.getAlbum_url());
				albumsImages.add(ImageIO.read(url));
			} catch (IOException e) {
				e.printStackTrace();
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
		g.drawString(people, (X_MARGIN + 40) + (380 / 2) - width / 2, 700 - metrics.getAscent());
		GraphicUtils.doChart(g, X_MARGIN + 40, 700 - 20, 400, 50, 5, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo, DESC_FONT.deriveFont(36f));

		int inicio_albums = X_MARGIN + 400 + 195 + 40;
		count = 0;
		int images_drawn = 0;

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
					pos_x = inicio_albums + pos[count - 1];
					baseline = 105 + 400 * (images_drawn / 2);
					break;
				}
				case 2: {
					pos_x = inicio_albums + 175;
					baseline = 105 + 400 * (images_drawn);
					break;
				}
				case 1: {
					pos_x = inicio_albums + 175;
					baseline = 105 + 200;
					break;
				}
				default: {
					pos_x = inicio_albums + 350 * (images_drawn % 2);
					baseline = 105 + 400 * (images_drawn / 2);
					break;
				}
			}
			g.drawImage(albumsImage, pos_x, baseline, 300, 300, null);
			baseline += 300;
			AlbumInfo albumInfo = albumInfoList.get(count - 1);
			String album = albumInfo.getAlbum();

			String play = Integer.toString(albumInfo.getPlays());

			Font ogFont = g.getFont();
			float sizeFont = ogFont.getSize();
			while ((width = g.getFontMetrics(g.getFont()).stringWidth(album)) > 300 && sizeFont > 18f) {
				g.setFont(g.getFont().deriveFont(sizeFont -= 2));
			}
			g.drawString(album, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent());
			g.setFont(ogFont);


			baseline += metrics.getAscent() + metrics.getDescent();
			width = metrics.stringWidth(play);
			int start = pos_x + (300 / 2) - width / 2;
			int finish = start + width;
			width += 25;

			g.drawString(play, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent());
			g.drawImage(lastFmLogo, finish, baseline + metrics.getAscent() - metrics.getDescent() - metrics.getLeading() - 8, null);
			images_drawn++;
		}

		int yline = 380;
		if (artistImageFill != null) {
			g.drawImage(Scalr.resize(artistImageFill, yline, Scalr.OP_ANTIALIAS), X_MARGIN + 40 + (400 - 380) / 2, 25, null);
		}
		width = metrics.stringWidth(artist);
		yline += metrics.getAscent() + metrics.getDescent() + metrics.getLeading() + 20;
		g.drawString(artist, X_MARGIN + 40 + (380 / 2) - width / 2, yline);

		ReturnNowPlaying myRow = new ReturnNowPlaying(1, user, artist, plays);
		myRow.setDiscordName(user);

		WrapperReturnNowPlaying wrapper1Row = new WrapperReturnNowPlaying(Collections.singletonList(myRow), 1, artist, artist);
		GraphicUtils.doChart(g, X_MARGIN + 40, yline + metrics.getAscent() - 20, 400, 50, 1, wrapper1Row, colorB1, colorB, lastFmLogo, false, DESC_FONT.deriveFont(36f));

		return canvas;
	}
}
