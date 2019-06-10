package main.ImageRenderer.Stealing;


import DAO.Entities.AlbumInfo;
import DAO.Entities.ArtistAlbums;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;
import main.ImageRenderer.GraphicUtils;
import main.ImageRenderer.NPMaker;
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

		} catch (IOException e) {
			return null;
		}


		Color colorB1 = GraphicUtils.getReadableColorBackgroundForFont(GraphicUtils.getFontColorBackground(canvas));
		Color colorB = GraphicUtils.getSurfaceColor(GraphicUtils.getFontColorBackground(canvas));
		FONT_COLOR = GraphicUtils.getBetter(GraphicUtils.getFontColorBackground(canvas));
		BufferedImage artistImageFill = GraphicUtils.getImageFromUrl(wrapperReturnNowPlaying.getUrl(), null);
		Graphics2D g = GraphicUtils.initArtistBackground(canvas, artistImageFill);

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
		g.drawString(people, (X_MARGIN + 40) + (300 / 2) - width / 2, 700 - metrics.getAscent());
		NPMaker.doChart(g, X_MARGIN + 40, 700 - 20, 300, 160, wrapperReturnNowPlaying, colorB1, colorB, lastFmLogo);

		int inicio_albums = 438;
		count = 0;
		int images_drawn = 0;

		for (BufferedImage albumsImage : albumsImages) {
			count++;
			if (albumsImage == null) {
				continue;
			}
			int pos_x = inicio_albums + 350 * (images_drawn % 2);
			int baseline = 105 + 400 * (images_drawn / 2);
			g.drawImage(albumsImage, pos_x, baseline, 300, 300, null);
			baseline += 300;
			AlbumInfo albumInfo = albumInfoList.get(count - 1);
			String album = albumInfo.getAlbum();

			String play = Integer.toString(albumInfo.getPlays());
			width = metrics.stringWidth(album);
			g.drawString(album, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent());
			baseline += metrics.getAscent() + metrics.getDescent();
			width = metrics.stringWidth(play);
			int start = pos_x + (300 / 2) - width / 2;
			int finish = start + width;
			width += 25;

			g.drawString(play, pos_x + (300 / 2) - width / 2, baseline + metrics.getAscent());
			g.drawImage(Scalr.resize(lastFmLogo, 30), finish, baseline + metrics.getAscent() - metrics.getDescent() - metrics.getLeading() - 8, null);
			images_drawn++;
		}

		int yline = 380;
		if (artistImageFill != null) {
			g.drawImage(Scalr.resize(artistImageFill, yline, Scalr.OP_ANTIALIAS), X_MARGIN, 25, null);
		}
		width = metrics.stringWidth(artist);
		yline += metrics.getAscent() + metrics.getDescent() + metrics.getLeading() + 20;
		g.drawString(artist, X_MARGIN + (380 / 2) - width / 2, yline);

		ReturnNowPlaying myRow = new ReturnNowPlaying(1, user, artist, plays);
		myRow.setDiscordName(user);

		WrapperReturnNowPlaying wrapper1Row = new WrapperReturnNowPlaying(Collections.singletonList(myRow), 1, artist, artist);
		NPMaker.doChart(g, X_MARGIN + 40, yline + metrics.getAscent() - 20, 300, 32, wrapper1Row, colorB1, colorB, lastFmLogo, false);

		return canvas;
	}
}
