package core.imagerenderer;

import core.Chuu;
import core.apis.last.entities.chartentities.PreComputedChartEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
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
    final Font START_FONT;
    private final boolean asideMode;
    private final StringFitter titleFitter;
    private final StringFitter subTitleFitter;
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
        titleFitter = new StringFitterBuilder(START_FONT_SIZE, imageSize - 5)
                .setStep(1)
                .setMinSize(lowerLimitStringSize)
                .setFitStrategy(asideMode ? StringFitter.FitStrategy.HEIGHT : StringFitter.FitStrategy.WIDTH)
                .setBaseFont(START_FONT)
                .setStyle(Font.BOLD).build();
        subTitleFitter = new StringFitterBuilder(START_FONT_SIZE - 2, imageSize - 5)
                .setStep(1)
                .setMinSize(lowerLimitStringSize)
                .setFitStrategy(asideMode ? StringFitter.FitStrategy.HEIGHT : StringFitter.FitStrategy.WIDTH)
                .setBaseFont(START_FONT)
                .setStyle(Font.PLAIN).build();
    }

    protected static OptionalInt maxWidth(BlockingQueue<UrlCapsule> queue, int imageHeight, int columns) {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = bufferedImage.createGraphics();
        int actualSize = columns * imageHeight;
        int heightPerItem = actualSize / queue.size();
        LinkedBlockingQueue<UrlCapsule> c = new LinkedBlockingQueue<>();
        queue.drainTo(c);
        OptionalInt max = Arrays.stream(c.toArray(UrlCapsule[]::new)).mapToInt(x -> {
            String join = x.getLines().stream().map(ChartLine::getLine).collect(Collectors.joining(" - "));
            Font font = GraphicUtils.chooseFont(join).deriveFont(Font.BOLD, imageHeight / columns == 300 ? 24 : 12);
            g1.setFont(font);
            while (g1.getFontMetrics().getStringBounds(join, g1).getHeight() >= heightPerItem) {
                g1.setFont(g1.getFont().deriveFont((float) g1.getFont().getSize() - 1.0f));
            }
            g1.setFont(font);
            return (int) g1.getFontMetrics().getStringBounds(join, g1).getWidth();
        }).max();
        g1.dispose();
        bufferedImage.flush();
        queue.addAll(c);
        return max;
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
        g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);
        g.setColor(temp);
        if (asideMode) {
            drawNeverEndingCharts(capsule, y, x, imageSize);
        } else {
            temp = g.getColor();
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

        Color temp = g.getColor();
        g.setColor(Color.WHITE);
        g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);
        g.setColor(temp);


        if (capsule.isDarkToWhite()) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillRect(x * imageSize, y * imageSize, imageSize, imageSize);
        g.setColor(temp);

        if (asideMode) {
            drawNeverEndingCharts(capsule, y, x, imageSize);
        } else {
            drawNames(capsule, y, x, g, imageSize, null);
        }
    }

    @Override
    public void run() {
        while (iterations.getAndDecrement() > 0) {

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

    private int calculateHeight(List<ChartLine> lines, Map<ChartLine, StringFitter.FontMetadata> mapToFill) {
        int height = 3;
        for (ChartLine chartLine : lines) {
            String line = chartLine.getLine();
            StringFitter.FontMetadata fontMetadata = chooseFont(line, chartLine.getType().equals(ChartLine.Type.TITLE));
            FontMetrics fontMetrics = g.getFontMetrics(fontMetadata.maxFont());
            FontMetrics metrics = g.getFontMetrics(START_FONT);
            height += metrics.getAscent() - metrics.getDescent() + 7;
            mapToFill.put(chartLine, fontMetadata);
        }
        return height;
    }


    private StringFitter.FontMetadata chooseFont(String string, boolean isTitle) {
        return isTitle ? this.titleFitter.getFontMetadata(g, string) : this.subTitleFitter.getFontMetadata(g, string);
    }

    private StringFitter.FontMetadata chooseFont(String string, int size) {
        return this.titleFitter.getFontMetadata(g, string, size);
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
        StringFitter.FontMetadata fontMetadata = chooseFont(join, heightPerLine);
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
            g.drawString(fontMetadata.atrribute().getIterator(), this.x * imageWidth + 5, (int) ((baseline + (v + 1) * x) + (1 * v)));
            g.setFont(ogFont);
            g.setColor(temp);
        }
    }

    void drawNames(UrlCapsule capsule, int y, int x, Graphics2D g, int imageWidth, BufferedImage image) {
        List<ChartLine> chartLines = capsule.getLines();
        if (chartLines.isEmpty()) {
            return;
        }
        Map<ChartLine, StringFitter.FontMetadata> map = new HashMap<>();
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
            StringFitter.FontMetadata font = map.get(chartLine);
            FontMetrics metric = g.getFontMetrics(font.maxFont());
            int nextIncrease = metric.getAscent() - metric.getDescent() + 7;
            int artistWidth = (int) font.bounds().getWidth();
            int xOffset = 5;
            String line = chartLine.getLine();
            accum += nextIncrease;
            if (image != null) {
                g.setColor(Color.WHITE);
                synchronized (this.g) {
                    GraphicUtils.drawStringChartly(g, font, xOffset, accum);
                }
            } else {
                if (capsule instanceof PreComputedChartEntity) {
                    g.setColor(((PreComputedChartEntity) capsule).isDarkToWhite() ? Color.WHITE : Color.BLACK);
                } else {
                    g.setColor(Color.BLACK);
                }
                synchronized (this.g) {
                    g.drawString(font.atrribute().getIterator(), x * imageSize + xOffset, y * imageSize + accum);
                }
            }
        }
    }

}
