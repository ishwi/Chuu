package main.ImageRenderer;

import de.androidpit.colorthief.ColorThief;
import main.ResultWrapper;
import main.Results;
import main.last.UserInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class imageRenderer {
	private static String pathNoImage = "C:\\Users\\Ishwi\\Pictures\\New folder\\818148bf682d429dc215c1705eb27b98.png";
	private static int PROFILE_IMAGE_SIZE = 100;
	private static int x_MAX = 600;
	private static int y_MAX = 500;

	public static BufferedImage generateTasteImage(ResultWrapper resultWrapper, List<UserInfo> userInfoLiust) {


		BufferedImage canvas = new BufferedImage(x_MAX, y_MAX, BufferedImage.TYPE_INT_RGB);

		List<BufferedImage> imageList = new ArrayList<>();


		Graphics2D g = canvas.createGraphics();
		g.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		for (UserInfo userInfo : userInfoLiust) {
			BufferedImage image = null;
			try {
				java.net.URL url = new java.net.URL(userInfo.getImage());
				imageList.add(ImageIO.read(url));
			} catch (IOException e) {
				try {
					imageList.add(ImageIO.read(new File(imageRenderer.pathNoImage)));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		int x = 0;
		int y = 20;
		int drawx1, drawx2;
		drawx1 = 30;
		drawx2 = canvas.getWidth() - 180;
		g.setColor(Color.LIGHT_GRAY);
		int rectangle_start_x = drawx1 + PROFILE_IMAGE_SIZE + 4;
		int rectangle_start_y = y + PROFILE_IMAGE_SIZE - 20;
		int rectangle_height = 18;
		int rectangle_width = drawx2 - drawx1 - PROFILE_IMAGE_SIZE - 8;

		g.drawRect(rectangle_start_x, rectangle_start_y, rectangle_width, rectangle_height);
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 10);

		g.setFont(font);


		for (BufferedImage image : imageList) {

			g.setColor(Color.LIGHT_GRAY);
			int drawx = x == 1 ? drawx2 : drawx1;

			g.drawImage(image, drawx, y, 100, 100, null);
			int stringx = x == 0 ? drawx + PROFILE_IMAGE_SIZE + 5 : drawx - 8 * 5;
			UserInfo userInfo = userInfoLiust.get(x);

			g.drawString("" + userInfo.getPlaycount(), stringx, rectangle_start_y + rectangle_height - 1);
			x++;
		}


		y = rectangle_start_y + 64 + 20;
		String a = String.valueOf(resultWrapper.getRows());
		int lengt = a.length();

		Font titleFont = new Font(Font.SERIF, Font.PLAIN, 21);

		g.setFont(titleFont);
		g.drawString("" + resultWrapper.getRows(), x_MAX / 2 - 28 * lengt / 2, y - 30);

		Font subtitle = new Font(Font.DIALOG, Font.PLAIN, 12);
		g.setFont(subtitle);

		g.drawString("common artists", x_MAX / 2 + 28 * lengt / 8 + 4, y - 30);
		Font titleFont22 = new Font(Font.SERIF, Font.BOLD, 14);

		g.setFont(titleFont22);

		for (Results item : resultWrapper.getResultList()) {
			g.setColor(Color.LIGHT_GRAY);

			String artistID = item.getArtistID();
			int countA = item.getCountA();
			int countB = item.getCountB();
			g.drawString("" + countA, 100, y);
			g.drawString(artistID, x_MAX / 2 - 5 * artistID.length() / 2, y);
			g.drawString("" + countB, x_MAX - 100, y);
			int halfDistance = x_MAX - 200;
			int ac = Math.round((float) countA / (float) (countA + countB) * halfDistance);
			int bc = Math.round((float) countB / (float) (countA + countB) * halfDistance);
			g.setColor(Color.orange);
			g.fillRect(x_MAX / 2 - ac / 2, y + 2, ac / 2, 5);
			g.setColor(Color.CYAN);
			g.fillRect(x_MAX / 2, y + 2, bc / 2, 5);
			y += 20;
		}

		return canvas;
	}

	private static Color getColor(BufferedImage image) {
		int[] color = ColorThief.getColor(image);

		return new Color(color[0], color[1], color[2]);

	}

	private static Color getAverageTwo(Color color1, Color color2) {
		float r1 = color1.getRed();
		float g1 = color1.getGreen();
		float b1 = color1.getBlue();
		float r2 = color2.getRed();
		float g2 = color2.getGreen();
		float b2 = color2.getBlue();


		Color a = new Color((int) sqrt((Math.pow(r1, 2) + Math.pow(r2, 2)) / 2), (int) sqrt((Math.pow(g1, 2) + Math.pow(g2, 2)) / 2), (int) sqrt((Math.pow(b1, 2) + Math.pow(b2, 2)) / 2));
		return a;
	}


}
