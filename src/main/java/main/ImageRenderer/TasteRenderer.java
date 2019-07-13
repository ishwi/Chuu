package main.ImageRenderer;

import DAO.Entities.ResultWrapper;
import DAO.Entities.Results;
import DAO.Entities.UserInfo;
import main.ImageRenderer.Stealing.GaussianFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class TasteRenderer {
	private static final int PROFILE_IMAGE_SIZE = 100;
	private static final int x_MAX = 600;
	private static final int y_MAX = 500;

	public static BufferedImage generateTasteImage(ResultWrapper resultWrapper, List<UserInfo> userInfoLiust) {

		BufferedImage canvas = new BufferedImage(x_MAX, y_MAX, BufferedImage.TYPE_INT_RGB);

		List<BufferedImage> imageList = new ArrayList<>();

		Graphics2D g = canvas.createGraphics();
		GraphicUtils.setQuality(g);

		a(g);

		//Gets Profile Images
		for (UserInfo userInfo : userInfoLiust) {
			BufferedImage image = null;
			try {
				java.net.URL url = new java.net.URL(userInfo.getImage());
				imageList.add(ImageIO.read(url));
			} catch (IOException e) {
				try {
					imageList.add(ImageIO.read(WhoKnowsMaker.class.getResourceAsStream("/noArtistImage.png")));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		//Init Of Variables
		Font artistFont = new Font("ROBOTO-REGULAR", Font.PLAIN, 21);
		Font numberFont = new Font("HEEBO-LIGHT", Font.PLAIN, 21);
		Font titleFont = new Font("HEEBO-LIGHT", Font.PLAIN, 23);
		Font scrobbleFont = new Font("HEEBO-LIGHT", Font.BOLD, 17);
		int startFont = 26;
		Font usernameFont = (new Font("ROBOTO-MEDIUM", Font.PLAIN, startFont));
		Font subtitle = new Font("ROBOTOCONDENSED-BOLD", Font.ITALIC, 12);

		int x = 0;
		int y = 20;
		int image1StartPosition, image2StartPosition;
		image1StartPosition = 20;
		image2StartPosition = canvas.getWidth() - PROFILE_IMAGE_SIZE - 20;
		g.setColor(Color.WHITE);
		//int rectangle_start_x = image1StartPosition + PROFILE_IMAGE_SIZE + 4;
		int rectangle_start_y = y + PROFILE_IMAGE_SIZE - 20;
		int rectangle_height = g.getFontMetrics().getHeight();
		int rectangle_width = image2StartPosition - image1StartPosition - PROFILE_IMAGE_SIZE - 8;

		float[] rgb1 = new float[3];
		Color.ORANGE.getRGBColorComponents(rgb1);
		Color colorA = new Color(rgb1[0], rgb1[1], rgb1[2], 0.1f);
		Color colorA1 = new Color(rgb1[0], rgb1[1], rgb1[2], 0.6f);

		float[] rgb2 = new float[3];
		Color.CYAN.getRGBColorComponents(rgb2);
		Color colorB = new Color(rgb2[0], rgb2[1], rgb2[2], 0.1f);
		Color colorB1 = new Color(rgb2[0], rgb2[1], rgb2[2], 0.6f);

		g.setFont(usernameFont);
		int widht1 = g.getFontMetrics().stringWidth(userInfoLiust.get(0).getUsername());
		int width2 = g.getFontMetrics().stringWidth(userInfoLiust.get(1).getUsername());
		int totalwidth = widht1 + width2 + 4;
		int disponibleSize = rectangle_width + 8;

		while (totalwidth >= disponibleSize) {
			startFont -= 2;
			usernameFont = new Font("ROBOTO-MEDIUM", Font.PLAIN, startFont);
			g.setFont(usernameFont);
			widht1 = g.getFontMetrics().stringWidth(userInfoLiust.get(0).getUsername());
			width2 = g.getFontMetrics().stringWidth(userInfoLiust.get(1).getUsername());
			totalwidth = widht1 + width2 + 4;
		}
		int totalCount = userInfoLiust.stream().mapToInt(UserInfo::getPlayCount).sum();

		//Draws Profile Images
		for (BufferedImage image : imageList) {
			int drawx;
			int nameStringPosition;
			Color color;
			int countStringPosition;
			int rectanglePosition;
			UserInfo userInfo = userInfoLiust.get(x);
			float percentage = (float) userInfo.getPlayCount() / totalCount;

			if (x == 0) {
				drawx = image1StartPosition;
				nameStringPosition = image1StartPosition + PROFILE_IMAGE_SIZE + 4;
				color = colorA.brighter();
				countStringPosition = image1StartPosition + PROFILE_IMAGE_SIZE + 5;
				rectanglePosition = countStringPosition - 1;
			} else {
				drawx = image2StartPosition;
				nameStringPosition = image2StartPosition - width2 - 4;
				color = colorB.brighter();
				countStringPosition = image2StartPosition - g.getFontMetrics()
						.stringWidth(String.valueOf(userInfo.getPlayCount())) - 5;
				rectanglePosition = (int) (image2StartPosition - percentage * rectangle_width) - 4;
			}
			g.setColor(color);
			g.drawImage(image, drawx, y, 100, 100, null);
			g.fillRect(rectanglePosition, rectangle_start_y, (int) (rectangle_width * percentage), rectangle_height);
			g.setColor(Color.WHITE);
			g.setFont(usernameFont);
			g.drawString(userInfo.getUsername(), nameStringPosition, 20 + PROFILE_IMAGE_SIZE / 2);
			g.setFont(scrobbleFont);
			g.drawString("" + userInfo.getPlayCount(), countStringPosition, rectangle_start_y + rectangle_height - 1);
			x++;

		}

		//Draws Common Artists
		y = rectangle_start_y + 64 + 20;
		String a = String.valueOf(resultWrapper.getRows());

		g.setFont(titleFont);
		int length = g.getFontMetrics().stringWidth(a);
		g.drawString("" + resultWrapper.getRows(), x_MAX / 2 - length / 2, y - 30);

		g.setFont(subtitle);

		g.drawString("common artists", x_MAX / 2 + length / 2 + 4, y - 30);

		//Draws Top 10
		for (Results item : resultWrapper.getResultList()) {

			String artistID = item.getArtistID();
			int countA = item.getCountA();
			int countB = item.getCountB();

			int halfDistance = x_MAX - 200;
			int ac = Math.round((float) countA / (float) (countA + countB) * halfDistance);
			int bc = Math.round((float) countB / (float) (countA + countB) * halfDistance);
			g.setColor(colorA1);
			g.fillRect(x_MAX / 2 - ac / 2, y + 3, ac / 2, 5);
			g.setColor(colorB1);
			g.fillRect(x_MAX / 2, y + 3, bc / 2, 5);

			g.setColor(Color.WHITE);
			g.setFont(numberFont);
			String strCountBString = String.valueOf(item.getCountB());

			int widthB = g.getFontMetrics().stringWidth(strCountBString);

			int countBStart = x_MAX - 100 - widthB;
			g.drawString("" + countA, 100, y);
			g.drawString("" + countB, countBStart, y);
			g.setFont(artistFont);
			int widthStr = g.getFontMetrics().stringWidth(item.getArtistID());
			g.drawString(artistID, x_MAX / 2 - (widthStr / 2), y);

			y += g.getFontMetrics().getHeight() + 5;
			System.out.println(g.getFontMetrics().getHeight());
		}
		g.dispose();
		return canvas;
	}

	private static void a(Graphics2D g) {
		BufferedImage bim;

		Properties properties = new Properties();

		try (InputStream in = TasteRenderer.class.getResourceAsStream("/" + "all.properties")) {
			properties.load(in);
			String path = properties.getProperty("WALLPAPER_FOLDER");
			File dir = new File(path);
			File[] files = dir.listFiles();
			Random rand = new Random();
			assert files != null;
			File file = files[rand.nextInt(files.length)];
			bim = ImageIO.read(file);
			bim = cropImage(bim);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		g.drawImage(bim, new GaussianFilter(45), 0, 0);


	}


	private static BufferedImage cropImage(BufferedImage src) {
		int height = src.getTileHeight();
		int width = src.getTileWidth();
		int limity = height - y_MAX;
		int limitx = width - x_MAX;
		Random rand = new Random();
		int x = rand.nextInt(limitx);
		int y = rand.nextInt(limity);
		RescaleOp op = new RescaleOp(.8f, 0, null);
		return op.filter(src.getSubimage(x, y, x_MAX, y_MAX), null);

	}
}