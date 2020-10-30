package core.imagerenderer;

import core.Chuu;
import core.apis.ExecutorsSingleton;
import dao.exceptions.ChuuServiceException;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CollageGenerator {
    public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<Pair<BufferedImage, Integer>> queue, ChartQuality chartQuality) {
        BufferedImage result;
        int imageType = BufferedImage.TYPE_INT_ARGB;

        switch (chartQuality) {
            case PNG_BIG:
                break;
            case JPEG_BIG:
                imageType = BufferedImage.TYPE_INT_RGB;
                break;
            case PNG_SMALL:
                break;
            case JPEG_SMALL:
                imageType = BufferedImage.TYPE_INT_RGB;
                break;
        }

        result = new BufferedImage(x * 800, y * 500, imageType);
        Graphics2D g = result.createGraphics();
        GraphicUtils.setQuality(g);

        AtomicInteger max = new AtomicInteger(queue.size());
        ExecutorService es = ExecutorsSingleton.getInstance();

        List<Callable<Object>> calls = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            calls.add(Executors.callable(new CollageQueue(g, x, y, max, false, false, queue)));
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
