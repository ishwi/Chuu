package core.imagerenderer;

import core.Chuu;
import core.apis.ExecutorsSingleton;
import core.apis.last.chartentities.UrlCapsule;
import dao.exceptions.ChuuServiceException;

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
            int optionalInt = ThreadQueue.maxWidth(queue, y * imageSize, y).orElse(0);
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
        AtomicInteger max = new AtomicInteger(queue.size());
        ExecutorService es = ExecutorsSingleton.getInstance();

        List<Callable<Object>> calls = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            calls.add(Executors.callable(new ThreadQueue(queue, g, x, y, max, imageSize == 150, asideMode)));
        }
        try {
            es.invokeAll(calls);
        } catch (InterruptedException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }

        g.dispose();
        return result;
    }
}
