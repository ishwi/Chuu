package main.ImageRenderer;

import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class NPMaker {

	private static final String pathNoImage = "C:\\Users\\Ishwi\\Pictures\\New folder\\818148bf682d429dc215c1705eb27b98.png";
	private static final int ALBUM_IMAGE_SIZE = 150;
	private static final int x_MAX = 600;
	private static final int y_MAX = 500;

	public static BufferedImage generateNP(WrapperReturnNowPlaying wrapperReturnNowPlaying,String discordName) {

		BufferedImage canvas = new BufferedImage(x_MAX, y_MAX, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = canvas.createGraphics();
		if (wrapperReturnNowPlaying.getRows() == 0) {

			return null;
		}

		BufferedImage image = null;
		try {
			java.net.URL url = new java.net.URL(wrapperReturnNowPlaying.getUrl());
			image = (ImageIO.read(url));

		} catch (IOException e) {
			try {
				image = ImageIO.read(new File(NPMaker.pathNoImage));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		int y = 10;
		g.drawImage(image,10,y,150,150,null);
		y += ALBUM_IMAGE_SIZE/2;
		g.setFont(new Font("Times New Roman Bold",Font.PLAIN,22));
		g.drawString("Who knows " + wrapperReturnNowPlaying.getArtist() + " in "+ discordName + "  \n" ,170,y);
		y  += g.getFontMetrics().getHeight() + 1;

		g.setFont(new Font("Times new Roman",Font.PLAIN,16));
		g.drawString(wrapperReturnNowPlaying.getRows() + (wrapperReturnNowPlaying.getRows()==1 ? " member has scrobbled " : " members has scrobbled ") + wrapperReturnNowPlaying.getArtist(),170,y);
		y+= g.getFontMetrics().getHeight() + 1;
		g.drawString("Top 10 is: ",170,y);
		y = ALBUM_IMAGE_SIZE + 50;
		List<ReturnNowPlaying> iter =  wrapperReturnNowPlaying.getReturnNowPlayings();
		int counter =1;
		for (ReturnNowPlaying returnNowPlaying : iter) {

			String a = counter++ + ". " + returnNowPlaying.getLastFMId() + " with " + returnNowPlaying.getPlaynumber() + " plays";
			int pixel_size = g.getFontMetrics().getHeight();

			g.drawString(a,x_MAX/3 ,y);
			y += g.getFontMetrics().getHeight() + 1;
		}
	return canvas;
	}

}
