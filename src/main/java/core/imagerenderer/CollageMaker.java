package core.imagerenderer;

import core.Chuu;
import core.apis.last.entities.chartentities.UrlCapsule;
import dao.exceptions.ChuuServiceException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CollageMaker {
    private static final int DEFAULT_SIZE = 300;
    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 12, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200));

    private CollageMaker() {
    }

    public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<UrlCapsule> queue, ChartQuality chartQuality, boolean asideMode) {
        BufferedImage result;
        int imageSize = DEFAULT_SIZE;
        int imageType = BufferedImage.TYPE_INT_ARGB;


        switch (chartQuality) {
            case PNG_BIG:
                break;
            case JPEG_BIG:
                imageType = BufferedImage.TYPE_INT_RGB;
                break;
            case PNG_SMALL:
                imageSize = 150;
                break;
            case JPEG_SMALL:
                imageSize = 150;
                imageType = BufferedImage.TYPE_INT_RGB;
                break;
        }

        if (asideMode) {
            ThreadQueue.WidthResult widthResult = ThreadQueue.maxWidth(queue, y * imageSize, y);
            int optionalInt = widthResult.width().orElse(0);
            queue = widthResult.queue();
            if (optionalInt != 0) {
                optionalInt += 50;
            }
            result = new BufferedImage(x * imageSize + optionalInt, y * imageSize, imageType);
        } else {
            result = new BufferedImage(x * imageSize, y * imageSize, imageType);

        }
        Graphics2D g = result.createGraphics();
        GraphicUtils.setQuality(g);

        if (asideMode) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, result.getWidth(), result.getHeight());
        }
        int size = queue.size();
        AtomicInteger max = new AtomicInteger(size);
        ReentrantLock lock = new ReentrantLock();


        java.util.List<Callable<Object>> calls = new ArrayList<>();
        int bound = Math.min(4, size);
        for (int i = 0; i < bound; i++) {
            calls.add(Executors.callable(new ThreadQueue(queue, g, x, y, max, imageSize == 150, asideMode, lock)));
        }
        try {
            threadPoolExecutor.invokeAll(calls);
        } catch (InterruptedException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }


        g.dispose();
        return result;
    }
}
