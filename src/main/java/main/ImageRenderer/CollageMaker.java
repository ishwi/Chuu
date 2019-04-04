package main.ImageRenderer;

import DAO.Entities.UrlCapsule;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CollageMaker {

	public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<UrlCapsule> queue) {

		BufferedImage result = new BufferedImage(
				x * 300, y * 300, //work these out
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = result.createGraphics();
		System.out.println("a");


		ExecutorService es = Executors.newCachedThreadPool();
		for (int i = 0; i < 4; i++)
			es.execute((new ThreadQueue(queue, g, x, y)));
		es.shutdown();
		try {
			boolean finished = es.awaitTermination(10, TimeUnit.MINUTES);
			System.out.println(finished);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

//                for (String  item : urls) {
//            BufferedImage image ;
//            URL url;
//            try {
//
//                url = new URL(item);
//                image = ImageIO.read(url);
//                g.drawImage(image,x,y,null);
//                x+=300;
//                if(x >=result.getWidth()){
//                    x = 0;
//                    y += image.getHeight();
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//        }
		g.dispose();
		return result;
	}
}
