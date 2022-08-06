package core.imagerenderer;

import core.Chuu;
import core.util.VirtualParallel;
import dao.exceptions.ChuuServiceException;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CollageGenerator {
    public static BufferedImage generateCollageThreaded(int x, int y, BlockingQueue<Pair<BufferedImage, Integer>> queue, ChartQuality chartQuality) {
        BufferedImage result;
        int imageType = BufferedImage.TYPE_INT_ARGB;

        switch (chartQuality) {
            case PNG_BIG:
            case PNG_SMALL:
                break;
            case JPEG_BIG:
            case JPEG_SMALL:
                imageType = BufferedImage.TYPE_INT_RGB;
                break;
        }

        result = new BufferedImage(x * 800, y * 500, imageType);

        Graphics2D g = result.createGraphics();
        GraphicUtils.setQuality(g);

        AtomicInteger max = new AtomicInteger(queue.size());
        ReentrantLock lock = new ReentrantLock();

        CollageQueue cq = new CollageQueue(g, x, y, max, false, false, queue, lock);
        try (var scope = new StructuredTaskScope<>()) {
            queue.forEach(ca ->
                    scope.fork(() -> {
                        cq.run0(ca);
                        return null;
                    }));
            scope.joinUntil(Instant.now().plus(1, ChronoUnit.MINUTES));
        } catch (TimeoutException | InterruptedException e) {
            VirtualParallel.handleInterrupt();
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }

        g.dispose();

        return result;
    }
}
