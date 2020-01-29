package core.imagerenderer;

import core.Chuu;
import core.apis.ExecutorsSingleton;
import dao.entities.UrlCapsule;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CollageMaker {
	private static final int DEFAULT_SIZE = 300;

	public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<UrlCapsule> queue, boolean writeTitles, boolean writePlays, boolean makeSmaller) {
		BufferedImage result;
		int imageSize = DEFAULT_SIZE;
		int imageType = BufferedImage.TYPE_INT_ARGB;

		if (makeSmaller) {
			imageSize = 150;
			imageType = BufferedImage.TYPE_INT_RGB;
		}

		result = new BufferedImage(x * imageSize, y * imageSize, imageType);

		Graphics2D g = result.createGraphics();
		GraphicUtils.setQuality(g);

		AtomicInteger max = new AtomicInteger(queue.size());
		ExecutorService es = ExecutorsSingleton.getInstanceUsingDoubleLocking();

		List<Callable<Object>> calls = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			calls.add(Executors.callable(new ThreadQueue(queue, g, x, y, max, writePlays, writeTitles, makeSmaller)));
		}
		try {
			es.invokeAll(calls);
		} catch (InterruptedException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}

		g.dispose();
		return result;
	}
}
