package core.imagerenderer;

import core.Chuu;
import core.apis.last.chartentities.PreComputedChartEntity;
import dao.entities.UrlCapsule;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


class ThreadQueue implements Runnable {
    final BlockingQueue<UrlCapsule> queue;
    final Graphics2D g;
    final int y;
    final int x;
    final AtomicInteger iterations;
    private final boolean asideMode;
    final Font START_FONT;
    int START_FONT_SIZE = 24;
    final Font JAPANESE_FONT = new Font("Yu Gothic", Font.PLAIN, START_FONT_SIZE);
    final Font KOREAN_FONT = new Font("Malgun Gothic", Font.PLAIN, START_FONT_SIZE);

    int lowerLimitStringSize = 14;
    int imageSize = 300;

    ThreadQueue(BlockingQueue<UrlCapsule> queue, Graphics2D g, int x, int y, AtomicInteger iterations, boolean makeSmaller, boolean asideMode) {
        this.queue = queue;
        this.g = g;
        this.y = y;
        this.x = x;
        this.iterations = iterations;
        this.asideMode = asideMode;
        if (makeSmaller) {
            this.imageSize = 150;
            START_FONT_SIZE = 12;
            lowerLimitStringSize = 7;
        }
        START_FONT = new Font("Noto Sans", Font.PLAIN, START_FONT_SIZE);
    }

    final void drawImage(BufferedImage image, UrlCapsule capsule, int x, int y) {
        if (image.getHeight() != imageSize || image.getWidth() != imageSize) {
            image = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, imageSize,
                    imageSize, Scalr.OP_ANTIALIAS);
        }
        drawImage(image, capsule);
        g.drawImage(image, x * imageSize, y * imageSize, null);
    }

    public void handleInvalidImage(UrlCapsule capsule, int x, int y) {
        Color temp = g.getColor();
        g.setColor(Color.WHITE);
        if (asideMode) {
            g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);
            drawNeverEndingCharts(capsule, y, x, imageSize);
        } else {
            g.setColor(Color.BLACK);
            drawNames(capsule, y, x, g, imageSize, null);
            g.setColor(temp);
        }
    }

    public void handleCapsule(UrlCapsule capsule, int x, int y) {
        if (capsule.getUrl() == null || capsule.getUrl().isBlank()) {
            handleInvalidImage(capsule, x, y);
            return;
        }
        BufferedImage image = GraphicUtils.getImage(capsule.getUrl());
        if (image == null) {
            handleInvalidImage(capsule, x, y);
            return;
        }
        drawImage(image, capsule, x, y);
        image.flush();
    }

    public void handlePreComputedCapsule(PreComputedChartEntity capsule, int x, int y) {
        BufferedImage image = capsule.getImage();
        if (image != null) {
            drawImage(image, capsule, x, y);
            image.flush();
        } else {
            handleInvalidPrecomputedImage(capsule, x, y);
        }
    }

    public void handleInvalidPrecomputedImage(PreComputedChartEntity capsule, int x, int y) {

        g.setColor(Color.WHITE);
        g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);


        Color temp = g.getColor();
        if (capsule.isDarkToWhite()) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);

        if (asideMode) {
            drawNeverEndingCharts(capsule, y, x, imageSize);
        } else {
            drawNames(capsule, y, x, g, imageSize, null);
        }
        g.setColor(temp);
    }

    @Override
    public void run() {
        while (iterations.getAndDecrement() > 0) {

            g.setFont(START_FONT);
            g.setColor(Color.BLACK);
            try {
                UrlCapsule capsule = queue.take();
                int pos = capsule.getPos();
                int y = (pos / this.x);
                int x = pos % this.x;
                if (capsule instanceof PreComputedChartEntity) {
                    handlePreComputedCapsule((PreComputedChartEntity) capsule, x, y);
                } else {
                    handleCapsule(capsule, x, y);
                }

            } catch (Exception e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }
        }


    }

    void drawImage(BufferedImage image, UrlCapsule capsule) {
        if (asideMode) {
            int pos = capsule.getPos();
            int y = (pos / this.x);
            int x = pos % this.x;
            drawNeverEndingCharts(capsule, y, x, imageSize);
        } else {
            Graphics2D gTemp = image.createGraphics();
            GraphicUtils.setQuality(gTemp);
            drawNames(capsule, 0, 0, gTemp, image.getWidth(), image);
            gTemp.dispose();
        }
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

    void drawNeverEndingCharts(UrlCapsule capsule, int y, int x, int imageWidth) {
        g.setColor(Color.WHITE);
        List<ChartLine> chartLines = capsule.getLines();
        if (chartLines.isEmpty()) {
            return;
        }
        int actualSize = this.y * imageWidth;
        int heightPerLine = actualSize / (this.x * this.y);
        int itemPerLine;

        String join = chartLines.stream().map(ChartLine::getLine).collect(Collectors.joining(" - "));
        Font font = chooseFont(join, false).deriveFont(Font.BOLD);

        while (g.getFontMetrics(font).getStringBounds(join, g).getHeight() >= heightPerLine) {
            font = font.deriveFont((float) font.getSize() - 1.0f);
        }
        int lineStart = y * imageSize;
        int lineEnd = lineStart + imageSize;
        itemPerLine = this.x;
        int sizeToUse = lineEnd - lineStart;
        double v = (0.9 * sizeToUse) / (float) (itemPerLine + 1);
        double baseline = lineStart + 0.05 * sizeToUse;
        synchronized (g) {
            Color temp = g.getColor();
            g.setColor(Color.WHITE);
            Font ogFont = g.getFont();
            g.setFont(font);
            g.drawString(join, this.x * imageWidth + 5, (int) ((baseline + (v + 1) * x) + (1 * v)));
            g.setFont(ogFont);
            g.setColor(temp);
        }
    }

    protected static OptionalInt maxWidth(BlockingQueue<UrlCapsule> queue, int imageHeight, int columns) {
        Graphics2D g1 = new BufferedImage(10000, 10000, BufferedImage.TYPE_INT_ARGB).createGraphics();
        int actualSize = columns * imageHeight;
        int heightPerItem = actualSize / queue.size();
        LinkedBlockingQueue<UrlCapsule> c = new LinkedBlockingQueue<>();
        queue.drainTo(c);
        OptionalInt max = Arrays.stream(c.toArray(UrlCapsule[]::new)).mapToInt(x -> {
            String join = x.getLines().stream().map(ChartLine::getLine).collect(Collectors.joining(" - "));
            Font font = GraphicUtils.chooseFont(join).deriveFont(Font.BOLD, imageHeight / columns == 300 ? 22 : 8);
            g1.setFont(font);


            while (g1.getFontMetrics().getStringBounds(join, g1).getHeight() >= heightPerItem) {
                g1.setFont(g1.getFont().deriveFont((float) g1.getFont().getSize() - 1.0f));
            }
            g1.setFont(font);
            return (int) g1.getFontMetrics().getStringBounds(join, g1).getWidth();
        }).max();
        queue.addAll(c);
        return max;
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
                if (capsule instanceof PreComputedChartEntity) {
                    g.setColor(((PreComputedChartEntity) capsule).isDarkToWhite() ? Color.WHITE : Color.BLACK);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.drawString(line, x * imageSize + xOffset, y * imageSize + accum);
            }
        }
    }

}
