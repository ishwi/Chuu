package main.ImageRenderer;

import main.ResultWrapper;
import main.Results;
import main.last.UserInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class imageRenderer {
	public static BufferedImage generateTasteImage(ResultWrapper resultWrapper, List<UserInfo> userInfoLiust) {
		int PROFILE_IMAGE_SIZE = 100;
		int x_MAX = 600;
		int y_MAX = 500;
		String NO_PROFILE_URL = "https://lastfm-img2.akamaized.net/i/u/avatar170s/818148bf682d429dc215c1705eb27b98";

		BufferedImage canvas = new BufferedImage(x_MAX, y_MAX, BufferedImage.TYPE_INT_RGB);
		Graphics g = canvas.getGraphics();

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

		Font font = new Font("Verdana", Font.BOLD, 10);
		g.setFont(font);
		for (UserInfo userInfo : userInfoLiust) {
			BufferedImage image = null;
			try {

				java.net.URL url = new java.net.URL(userInfo.getImage());
				image = ImageIO.read(url);

			} catch (IOException e) {
				java.net.URL url = null;
				try {
					url = new java.net.URL(NO_PROFILE_URL);
					image = ImageIO.read(url);
				} catch (MalformedURLException e1) {
					throw new RuntimeException();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			g.setColor(Color.LIGHT_GRAY);
			int drawx = x == 1 ? drawx2 : drawx1;
			g.drawImage(image, drawx, y, 100, 100, null);
			int stringx = x == 0 ? drawx + PROFILE_IMAGE_SIZE + 5 : drawx - 8 * 5;

			g.drawString("" + userInfo.getPlaycount(), stringx, rectangle_start_y + rectangle_height - 1);
			x++;
		}

		String a = String.valueOf(resultWrapper.getRows());
		int lengt = a.length();

		Font titleFont = new Font(Font.SERIF, Font.PLAIN, 21);

		g.setFont(titleFont);
		g.drawString("" + resultWrapper.getRows(), x_MAX / 2 - 28 * lengt / 2, rectangle_start_y + 64 + 20);

		Font subtitle = new Font(Font.DIALOG, Font.PLAIN, 12);
		g.setFont(subtitle);
		g.drawString("common artists", x_MAX / 2 + 28 * lengt / 4 + 4, rectangle_start_y + 64 + 20);

		for (Results item : resultWrapper.getResultList()) {

		}

		return canvas;
	}
}
