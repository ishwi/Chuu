package core.imagerenderer;

import core.apis.last.entities.chartentities.ArtistChart;
import core.apis.last.entities.chartentities.UrlCapsule;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

public class ThumbsMaker {

    public static BufferedImage generate(List<String> urls) {
        if (urls.isEmpty()) {
            return null;
        }
        if (urls.size() > 1 && urls.size() < 4) {
            urls = urls.stream().limit(1).toList();
        } else if (urls.size() > 4 && urls.size() < 9) {
            urls = urls.stream().limit(4).toList();
        } else {
            urls = urls.stream().limit(9).toList();
        }
        int size = urls.size();
        BlockingQueue<UrlCapsule> capsules = new ArrayBlockingQueue<>(size);
        BiFunction<Integer, String, UrlCapsule> factory = (i, s) -> new ArtistChart(s, i, null, null, 0, false, false, false);
        int imageSize = switch (size) {
            case 1 -> 300;
            case 2, 3, 4, 5, 6, 7, 8 -> 150;
            case 9 -> 100;
            default -> throw new IllegalStateException("Unexpected value: " + size);
        };
        for (int i = 0; i < urls.size(); i++) {
            UrlCapsule capsule = factory.apply(i, urls.get(i));
            capsules.add(capsule);
        }
        int x = (int) Math.floor(Math.sqrt(size));

        return CollageMaker.generateCollageThreaded(x, x, capsules, ChartQuality.PNG_BIG, false);
    }
}

