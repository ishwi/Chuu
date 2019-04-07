package main.ImageRenderer;

import DAO.Entities.UrlCapsule;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CollageMaker {

	public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<UrlCapsule> queue) {

		BufferedImage result = new BufferedImage(
				x * 300, y * 300, //work these out
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = result.createGraphics();
		GraphicUtils.setQuality(g);

		AtomicInteger max = new AtomicInteger(queue.size());
		ExecutorService es = Executors.newCachedThreadPool();
		for (int i = 0; i < 1; i++)
			es.execute((new ThreadQueue(queue, g, x, y, max)));
		es.shutdown();
		try {
			boolean finished = es.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		g.dispose();
		return result;
	}
}
