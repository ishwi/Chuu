package main.ImageRenderer;

import DAO.Entities.FullAlbumEntity;
import DAO.Entities.Track;
import main.Chuu;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public class TrackDistributor {
	private static final int TILE_NUMBER = 17;
	private static final int TILE_SIZE = 45;
	private static final int WIDTH_CONSTANT = 935;
	private static final int HEIGHT_CONSTANT = 390;
	private static final int HEIGHT_TITLES = 25;
	private static final int HEIGHT_BOTTOM = 0;
	private static final int FONT_SIZE = 30;
	private static final Font NORMAL_FONT = new Font("Noto Sans Display SemiBold", Font.PLAIN, FONT_SIZE);
	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP Regular", Font.PLAIN, FONT_SIZE);

	private static final BufferedImage[] corners;
	private static final BufferedImage[] sides;
	private static final BufferedImage tile;
	private static final BufferedImage side;
	private static final BufferedImage corner;
	private static final BufferedImage template;
	private static final BufferedImage noalbumImage;
	private static final List<List<Color>> palettes;


	static {
		try (InputStream in = BandRendered.class.getResourceAsStream("/images/corner.png")) {
			corner = ImageIO.read(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException();
		}
		try (InputStream in = BandRendered.class.getResourceAsStream("/images/side.png")) {
			side = ImageIO.read(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException();
		}

		try (InputStream in = BandRendered.class.getResourceAsStream("/images/tile.png")) {
			tile = ImageIO.read(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException();
		}

		try (InputStream in = BandRendered.class.getResourceAsStream("/images/noArtistImage.png")) {
			noalbumImage = ImageIO.read(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException();
		}
		try (InputStream in = BandRendered.class.getResourceAsStream("/images/template.png")) {
			template = ImageIO.read(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException();
		}

		corners = new BufferedImage[]{corner, Scalr.rotate(corner, Scalr.Rotation.CW_90), Scalr.rotate(corner, Scalr.Rotation.CW_270), Scalr.rotate(corner, Scalr.Rotation.CW_180)};
		sides = new BufferedImage[]{side, Scalr.rotate(side, Scalr.Rotation.CW_180), Scalr.rotate(side, Scalr.Rotation.CW_270), Scalr.rotate(side, Scalr.Rotation.CW_90)};
		palettes = initPalettes();
	}

	private static List<List<Color>> initPalettes() {
		List<Color> colors = new ArrayList<>();
		colors.add(Color.decode("#769fcd"));
		colors.add(Color.decode("#b9d7ea"));
		colors.add(Color.decode("#d6e6f2"));
		colors.add(Color.decode("#f7fbfc"));

		List<Color> colors2 = new ArrayList<>();
		colors2.add(Color.decode("#ffcfdf"));
		colors2.add(Color.decode("#fefdca"));
		colors2.add(Color.decode("#e0f9b5"));
		colors2.add(Color.decode("#a5dee5"));

		List<Color> colors3 = new ArrayList<>();
		colors3.add(Color.decode("#e4f9f5"));
		colors3.add(Color.decode("#30e3ca"));
		colors3.add(Color.decode("#11999e"));
		colors3.add(Color.decode("#40514e"));

		List<Color> colors4 = new ArrayList<>();
		colors4.add(Color.decode("#f9ecec"));
		colors4.add(Color.decode("#f0d9da"));
		colors4.add(Color.decode("#c8d9eb"));
		colors4.add(Color.decode("#ecf2f9"));

		List<Color> colors5 = new ArrayList<>();
		colors5.add(Color.decode("#ffe6eb"));
		colors5.add(Color.decode("#defcfc"));
		colors5.add(Color.decode("#cbf1f5"));
		colors5.add(Color.decode("#a6e3e9"));

		List<Color> colors6 = new ArrayList<>();
		colors6.add(Color.decode("#ececec"));
		colors6.add(Color.decode("#9fd3c7"));
		colors6.add(Color.decode("#385170"));
		colors6.add(Color.decode("#142d4c"));

		List<List<Color>> returnedList = new ArrayList<>();
		returnedList.add(colors);
		returnedList.add(colors2);
		returnedList.add(colors3);
		returnedList.add(colors4);
		returnedList.add(colors5);
		returnedList.add(colors6);

		return returnedList;
	}

	public static BufferedImage drawImage(FullAlbumEntity fae) {
		List<Track> trackList = fae.getTrackList();
		int trackCount = trackList.size();

		Optional<Track> max = trackList.stream().max(Comparator.comparingInt(Track::getPlays));
		assert max.isPresent();

		int maxList = max.get().getPlays();

		Font font;
		if (trackList.stream().anyMatch(x -> NORMAL_FONT.canDisplayUpTo(x.getName()) != -1) || (NORMAL_FONT
				.canDisplayUpTo(fae.getArtist()) != -1) || (NORMAL_FONT.canDisplayUpTo(fae.getAlbum()) != -1)) {
			font = JAPANESE_FONT;
		} else {
			font = NORMAL_FONT;
		}

		//Background image set up
		BufferedImage artistImageFill = GraphicUtils
				.getImageFromUrl(fae.getArtistUrl(), null);

		//Main Image
		BufferedImage dist = new BufferedImage(WIDTH_CONSTANT, HEIGHT_CONSTANT + HEIGHT_TITLES + HEIGHT_BOTTOM + TILE_SIZE * trackCount, BufferedImage.TYPE_INT_ARGB);
		//Album Image
		BufferedImage albumImage = GraphicUtils.getImageFromUrl(fae.getAlbumUrl(), noalbumImage);

		//Image Artist
		Graphics2D g = GraphicUtils.initArtistBackground(dist, artistImageFill);
		g.setFont(font);
		Font ogFont = font;

		//Upper Template Part
		g.drawImage(template, 0, 0, null);
		GraphicUtils.drawStringNicely(g, fae.getArtist(), 397, 160, dist);
		GraphicUtils.drawStringNicely(g, "Artist", 397, 199, dist);

		GraphicUtils.drawStringNicely(g, fae.getAlbum(), 397, 235, dist);
		GraphicUtils.drawStringNicely(g, "Album", 397, 274, dist);

		g.drawImage(Scalr.resize(albumImage, 330, 330), 22, 22, null);

		int startingPoint = HEIGHT_CONSTANT;
		BufferedImage correspondingTile = null;

		Integer[] stepArr = new Integer[TILE_NUMBER];
		stepArr[0] = 1;
		int step = maxList / TILE_NUMBER;

		for (int i = 1; i < stepArr.length; i++) {
			stepArr[i] = step * i;
		}

		int cornerIndex = 0;
		int trackListLength = trackCount - 1;

		//Track position -> counter
		int counter = 0;
		Random rand = new Random();
		List<Color> palette = palettes.get(new Random().nextInt(palettes.size() - 1));
		for (Track track : trackList) {
			float fontSize = FONT_SIZE;

			int trackName = g.getFontMetrics().stringWidth(track.getName());

			while (trackName > 200 && (fontSize -= 2) > 8f) {
				font = g.getFont().deriveFont(fontSize);
				g.setFont(font);
				trackName = g.getFontMetrics().stringWidth(track.getName());
			}

			GraphicUtils.drawStringNicely(g, track.getName(), 15 + (200 / 2) - trackName / 2, startingPoint +
							(TILE_SIZE - 5 - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent()
					, dist);
			g.setFont(ogFont);

			//i = row position
			for (int i = 0; i < TILE_NUMBER; i++) {
				//We are on a corner
				Color color = palette.get(rand.nextInt(palette.size() - 1));
				if ((i == 0 || i == TILE_NUMBER - 1) && (counter == 0 || counter == trackListLength)) {
					correspondingTile = corners[cornerIndex++];
				}
				//identify sides

				//top side
				else if (counter == 0) {
					correspondingTile = sides[0];

				} else if (counter == trackListLength) {
					correspondingTile = sides[1];

				} else if (i == 0) {
					correspondingTile = sides[2];

				} else if (i == TILE_NUMBER - 1) {
					correspondingTile = sides[3];
				}
				//inside
				else
					correspondingTile = tile;

				if (track.getPlays() > stepArr[i]) {
					correspondingTile = GraphicUtils.copyImage(correspondingTile);
					Graphics2D graphics = correspondingTile.createGraphics();
					graphics.setColor(color);
					graphics.fillRect(5, 5, 35, 35);
					graphics.dispose();
				}
				g.drawImage(correspondingTile, 15 + 200 + i * (TILE_SIZE - 5), startingPoint, null);

			}
			counter++;
			startingPoint += TILE_SIZE - 5;
		}

		g.setFont(NORMAL_FONT.deriveFont(28f));
		for (int i = 0; i < TILE_NUMBER; i++) {
			GraphicUtils.drawStringNicely(g, Integer
					.toString(stepArr[i]), 215 + i * (TILE_SIZE - 5) + ((TILE_SIZE - 5) / 3), startingPoint + TILE_SIZE, dist);
		}
		g.dispose();
		return dist;


	}
}
