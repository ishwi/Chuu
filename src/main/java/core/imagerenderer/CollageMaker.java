package core.imagerenderer;

import core.Chuu;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.util.VirtualParallel;
import dao.exceptions.ChuuServiceException;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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

        ThreadQueue tq = new ThreadQueue(queue, g, x, y, max, imageSize == 150, asideMode, lock);
        try (var scope = new StructuredTaskScope<>()) {
            queue.forEach(ca ->
                    scope.fork(() -> {
                        tq.run0(ca);
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
