package main.last;

import DAO.Entities.UrlCapsule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
//
//BufferedImage result = new BufferedImage(
//        1500   ,1500, //work these out
//        BufferedImage.TYPE_INT_RGB);
//        Graphics g = result.getGraphics();
public class ThreadQueue implements Runnable {
    private final BlockingQueue<UrlCapsule> queue;
    private final  Graphics g;
	private final int y;
	private final int x;

	ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics g, int x, int y) {
        this.queue = queue;
        this.g = g;
		this.x = x;
		this.y = y;

    }

    @Override
    public void run() {
        while (!queue.isEmpty()) {
            try {
                UrlCapsule encapsuler = queue.take();
                BufferedImage image;
                URL url;
                try {
                    int pos = encapsuler.getPos();
	                int y = (int) Math.floor(pos / this.x);
	                int x = pos % this.x;
                    url = new URL(encapsuler.getUrl());
                    image = ImageIO.read(url);
                    g.drawImage(image, x*300, y*300, null);
	                System.out.println(x + "                " + y);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println( "\n \n" + encapsuler.getUrl() + encapsuler.getPos()  + "\n \n ");
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	    System.out.println("\n \n \n\n\n\n\n\n\n           THREAD FINISHED             \n \n \n\n\n\n\n\n\n");

    }
}
