package core.imagerenderer;

import core.Chuu;
import dao.entities.FullAlbumEntity;
import dao.entities.Track;
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
	private static final Font JAPANESE_FONT = new Font("Noto Serif CJK JP", Font.PLAIN, FONT_SIZE);


	private static final BufferedImage template;
	private static final BufferedImage noalbumImage;
	private static final List<Color> lightPalettes;

	static {

		noalbumImage = GraphicUtils.noArtistImage;

		try (InputStream in = BandRendered.class.getResourceAsStream("/images/template.png")) {
			template = ImageIO.read(in);
		} catch (IOException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException();
		}
		lightPalettes = initLightPalettes();
	}

	private static List<Color> initLightPalettes() {
		List<Color> colors = new ArrayList<>();
		colors.add(Color.decode("#769fcd"));
		colors.add(Color.decode("#b9d7ea"));
		colors.add(Color.decode("#d6e6f2"));
		colors.add(Color.decode("#f7fbfc"));

		colors.add(Color.decode("#ffcfdf"));
		colors.add(Color.decode("#fefdca"));
		colors.add(Color.decode("#e0f9b5"));
		colors.add(Color.decode("#a5dee5"));

		colors.add(Color.decode("#e4f9f5"));
		colors.add(Color.decode("#30e3ca"));
		colors.add(Color.decode("#11999e"));
		colors.add(Color.decode("#40514e"));

		colors.add(Color.decode("#f9ecec"));
		colors.add(Color.decode("#f0d9da"));
		colors.add(Color.decode("#c8d9eb"));
		colors.add(Color.decode("#ecf2f9"));

		colors.add(Color.decode("#ffe6eb"));
		colors.add(Color.decode("#defcfc"));
		colors.add(Color.decode("#cbf1f5"));
		colors.add(Color.decode("#a6e3e9"));

		colors.add(Color.decode("#ececec"));
		colors.add(Color.decode("#9fd3c7"));
		colors.add(Color.decode("#385170"));
		colors.add(Color.decode("#142d4c"));

		return colors;
	}

	public static BufferedImage drawImage(FullAlbumEntity fae, boolean grid) {
		List<Track> trackList = fae.getTrackList();
		int trackCount = trackList.size();

		Optional<Track> max = trackList.stream().max(Comparator.comparingInt(Track::getPlays));
		assert max.isPresent();

		int maxList = max.get().getPlays();
		if (maxList == 0) {
			maxList = 1;
		}

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
		BufferedImage dist = new BufferedImage(WIDTH_CONSTANT, HEIGHT_CONSTANT + HEIGHT_BOTTOM + (TILE_SIZE) * trackCount + 15, BufferedImage.TYPE_INT_ARGB);
		//Album Image
		BufferedImage albumImage = GraphicUtils.getImageFromUrl(fae.getAlbumUrl(), noalbumImage);

		//Image Artist
		Graphics2D g = GraphicUtils.initArtistBackground(dist, artistImageFill);

		g.setFont(font);

		//Upper Template Part
		g.drawImage(template, 0, 0, null);
		GraphicUtils.drawStringNicely(g, fae.getArtist(), 397, 160, dist);
		GraphicUtils.drawStringNicely(g, "Artist", 397, 199, dist);

		GraphicUtils.drawStringNicely(g, fae.getAlbum(), 397, 235, dist);
		GraphicUtils.drawStringNicely(g, "Album", 397, 274, dist);

		g.drawImage(Scalr.resize(albumImage, 330, 330), 22, 22, null);

		doHistContent(g, maxList, dist, trackList);

		g.dispose();
		return dist;


	}

	private static void doHistContent(Graphics2D g, int maxList, BufferedImage dist, List<Track> trackList) {
		Font ogFont = g.getFont();
		Font font;
		Random rand = new Random();

		int x_limit = dist.getWidth();
		int y_limit = dist.getHeight() - HEIGHT_CONSTANT;

		Color color = lightPalettes.get(new Random().nextInt(lightPalettes.size() - 1));
		//Get a estimate of the average colour of the background
		Color[] a = new Color[15];
		for (int i = 0; i < 15; i++) {
			int rgb = dist.getRGB(rand.nextInt(x_limit), rand.nextInt(y_limit) + HEIGHT_CONSTANT);
			a[i] = (new Color(rgb));
		}
		Color betterCollection = GraphicUtils.getBetter(a);
		if (betterCollection.equals(Color.white))
			color = color.darker().darker();

		//I like transparency
		g.setColor(GraphicUtils.makeMoreTransparent(color, 0.7f));

		int startingPoint = HEIGHT_CONSTANT;
		OptionalInt max = trackList.stream().map(Track::getName)
				.mapToInt(x -> g.getFontMetrics().stringWidth(x))
				.max();
		int realMax = Math.min(452, max.orElse(400));
		int extra = 45;

		int minimunAmmount = realMax + extra;
		for (Track track : trackList) {
			float fontSize = FONT_SIZE;

			int trackName = g.getFontMetrics().stringWidth(track.getName());

			while (trackName > 400 && (fontSize -= 2) > 8f) {
				font = g.getFont().deriveFont(fontSize);
				g.setFont(font);
				trackName = g.getFontMetrics().stringWidth(track.getName());
			}

			int rectWidth = (int) (minimunAmmount + (905 - minimunAmmount) * (float) track.getPlays() / maxList);
			g.fillRect(15, startingPoint, rectWidth, 38);

			GraphicUtils.drawStringNicely(g, track.getName(), 25, startingPoint +
							(TILE_SIZE - 5 - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent()
					, dist);
			g.setFont(ogFont);

			String plays = String.valueOf(track.getPlays());

			GraphicUtils.drawStringNicely(g, plays, 15 + rectWidth - g.getFontMetrics()
							.stringWidth(plays) - 5, startingPoint +
							(TILE_SIZE - 5 - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent()
					, dist);
			startingPoint += TILE_SIZE;

		}

	}

}
