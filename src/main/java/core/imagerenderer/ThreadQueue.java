package core.imagerenderer;

import core.Chuu;
import dao.entities.UrlCapsule;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


class ThreadQueue implements Runnable {
    private final BlockingQueue<UrlCapsule> queue;
    private final Graphics2D g;
    private final int y;
    private final int x;
    private final AtomicInteger iterations;
    private final Font START_FONT;
    private int START_FONT_SIZE = 24;
    private final Font JAPANESE_FONT = new Font("Yu Gothic", Font.PLAIN, START_FONT_SIZE);
    private final Font KOREAN_FONT = new Font("Malgun Gothic", Font.PLAIN, START_FONT_SIZE);


    private int lowerLimitStringSize = 14;
    private int imageSize = 300;

    ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations) {
        this.queue = queue;
        this.g = g;
        this.x = x;
        this.y = y;
        this.iterations = iterations;
        START_FONT = new Font("Noto Sans", Font.PLAIN, START_FONT_SIZE);

    }

    ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations, boolean makeSmaller) {
        this.queue = queue;
        this.g = g;
        this.y = y;
        this.x = x;
        this.iterations = iterations;
        if (makeSmaller) {
            this.imageSize = 150;
            START_FONT_SIZE = 12;
            lowerLimitStringSize = 7;
        }
        START_FONT = new Font("Noto Sans", Font.PLAIN, START_FONT_SIZE);
    }


    @Override
    public void run() {
        while (iterations.getAndDecrement() > 0) {

            g.setFont(START_FONT);
            g.setColor(Color.BLACK);

            try {
                UrlCapsule capsule = queue.take();
                BufferedImage image;
                URL url;
                int pos = capsule.getPos();
                int y = (pos / this.x);
                int x = pos % this.x;

                try {
                    url = new URL(capsule.getUrl());
                    image = ImageIO.read(url);
                    if (image.getHeight() != imageSize || image.getWidth() != imageSize) {

                        image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, imageSize,
                                imageSize, Scalr.OP_ANTIALIAS);
                    }
                    drawImage(image, capsule);
                    g.drawImage(image, x * imageSize, y * imageSize, null);

                } catch (IOException e) {
                    Color temp = g.getColor();
                    g.setColor(Color.WHITE);
                    g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);
                    g.setColor(Color.BLACK);
                    drawNames(capsule, y, x, g, imageSize, null);
                    g.setColor(temp);
                }
            } catch (Exception e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }
        }


    }

    private void drawImage(BufferedImage image, UrlCapsule capsule) {
        Graphics2D gTemp = image.createGraphics();
        GraphicUtils.setQuality(gTemp);

        drawNames(capsule, 0, 0, gTemp, image.getWidth(), image);
        gTemp.dispose();
    }

    private int calculateHeight(List<ChartLine> lines, Map<ChartLine, Font> mapToFill) {
        int height = 3;
        for (ChartLine chartLine : lines) {
            String line = chartLine.getLine();
            Font fontToUse = chooseFont(line, chartLine.getType().equals(ChartLine.Type.TITLE));
            FontMetrics metrics = g.getFontMetrics(START_FONT);
            height += metrics.getAscent() - metrics.getDescent() + 7;
            mapToFill.put(chartLine, fontToUse);
        }
        return height;
    }


    private Font chooseFont(String string, boolean isTitle) {
        Font font = START_FONT;
        if (font.canDisplayUpTo(string) != -1) {
            font = JAPANESE_FONT;
            if (font.canDisplayUpTo(string) != -1) {
                font = KOREAN_FONT;
                if (font.canDisplayUpTo(string) != -1) {
                    font = START_FONT;
                }
            }
        }
        return font.deriveFont(isTitle ? Font.BOLD : Font.PLAIN, isTitle ? START_FONT_SIZE : START_FONT_SIZE - 2);
    }

    void drawNames(UrlCapsule capsule, int y, int x, Graphics2D g, int imageWidth, BufferedImage image) {
        List<ChartLine> chartLines = capsule.getLines();
        if (chartLines.isEmpty()) {
            return;
        }
        Map<ChartLine, Font> map = new HashMap<>();
        int gradientHeight = calculateHeight(chartLines, map);
        int oneLine = gradientHeight / chartLines.size();
        if (image != null) {
            GradientPaint gp1 = new GradientPaint(0, gradientHeight + oneLine, GraphicUtils.makeMoreTransparent(Color.BLACK, 0.0f), 0, 0, GraphicUtils.makeMoreTransparent(Color.BLACK, 0.6f), false);
            g.setPaint(gp1);
            g.fillRect(0, 0, image.getWidth(), gradientHeight + oneLine);
        }
        int accum = 3;

        for (ChartLine chartLine : chartLines) {
            int fontSize = START_FONT_SIZE;
            Font font = map.get(chartLine);
            g.setFont(font);
            FontMetrics metric = g.getFontMetrics();
            int nextIncrease = metric.getAscent() - metric.getDescent() + 7;
            int xOffset = 5;
            String line = chartLine.getLine();
            int artistWidth = g.getFontMetrics().stringWidth(line);
            while (artistWidth > (imageWidth - 5) && fontSize-- > lowerLimitStringSize) {
                font = font.deriveFont((float) fontSize);
                g.setFont(font);
                artistWidth = g.getFontMetrics().stringWidth(line);
            }
            accum += nextIncrease;
            if (image != null) {
                g.setColor(Color.WHITE);
                GraphicUtils.drawStringChartly(g, line, xOffset, accum);
            } else {
                g.setColor(Color.BLACK);
                g.drawString(line, x * imageSize + xOffset, y * imageSize + accum);
            }
        }
    }

    private Color getBetter(Color color) {
        double y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
        return y < 128 ? Color.WHITE : Color.BLACK;

    }

}
