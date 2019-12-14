package core.imagerenderer;


import dao.entities.UrlCapsule;
import core.Chuu;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GuildMaker {

	public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<UrlCapsule> queue) {

		BufferedImage result = new BufferedImage(
				x * 300, y * 300, //work these out
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = result.createGraphics();
		GraphicUtils.setQuality(g);

		AtomicInteger max = new AtomicInteger(queue.size());
		ExecutorService es = Executors.newCachedThreadPool();
		for (int i = 0; i < 2; i++)
			es.execute((new ThreadQueueGuild(queue, g, x, y, max)));
		es.shutdown();
		try {
			es.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}

		g.dispose();
		return result;
	}
}

