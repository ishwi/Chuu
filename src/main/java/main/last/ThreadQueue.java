package main.last;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
//
//BufferedImage result = new BufferedImage(
//        1500   ,1500, //work these out
//        BufferedImage.TYPE_INT_RGB);
//        Graphics g = result.getGraphics();
public class ThreadQueue implements Runnable {
    private final BlockingQueue<UrlCapsule> queue;
    private final  Graphics g;

    ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics g) {
        this.queue = queue;
        this.g = g;

    }

    @Override
    public void run() {
        while (!queue.isEmpty()) {
            try {
                UrlCapsule encapsuler = queue.take();
                BufferedImage image;
                URL url;
                try {
                    int pos = encapsuler.getPos();
                    int x = Math.round(pos / 5);
                    int y = pos % 5;
                    url = new URL(encapsuler.getUrl());
                    image = ImageIO.read(url);
                    g.drawImage(image, x*300, y*300, null);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println( "\n \n" + encapsuler.getUrl() + encapsuler.getPos()  + "\n \n ");
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
