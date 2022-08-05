package core.imagerenderer;

import core.Chuu;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CollageQueue extends ThreadQueue {
    private final BlockingQueue<Pair<BufferedImage, Integer>> bufferedImages;


    CollageQueue(Graphics2D g, int x, int y, AtomicInteger iterations, boolean makeSmaller, boolean asideMode, BlockingQueue<Pair<BufferedImage, Integer>> bufferedImages, ReentrantLock lock) {
        super(null, g, x, y, iterations, makeSmaller, asideMode, lock);
        this.bufferedImages = bufferedImages;
        this.imageSize = 400;
    }

    @Override
    public void run() {
        while (iterations.getAndDecrement() > 0) {
            try {
                Pair<BufferedImage, Integer> take = bufferedImages.take();
                run0(take);
            } catch (Exception e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }

        }
    }

    public void run0(Pair<BufferedImage, Integer> take) {
        try {
            BufferedImage image = take.getLeft();
            int pos = take.getRight();
            int y = (pos / this.x);
            int x = pos % this.x;
            handleImage(image, x, y);
        } catch (Exception e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
    }

    public void handleImage(BufferedImage image, int x, int y) {
        int imageWidth = 800;
        int imageHeight = 500;
        g.drawImage(image, x * imageWidth, y * imageHeight, null);
    }
}
