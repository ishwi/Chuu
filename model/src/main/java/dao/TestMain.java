//package dao;
//
//import core.apis.last.ConcurrentLastFM;
//import core.apis.last.LastFMFactory;
//import core.apis.last.TopEntity;
//import core.apis.last.entities.chartentities.AlbumChart;
//import core.apis.last.entities.chartentities.PreComputedByBrightness;
//import dao.exceptions.LastFmException;
//import core.imagerenderer.ChartQuality;
//import core.imagerenderer.CollageMaker;
//import core.imagerenderer.GraphicUtils;
//import core.imagerenderer.stealing.colorpicker.ColorThiefCustom;
//import core.parsers.params.ChartParameters;
//import core.apis.last.entities.chartentities.UrlCapsule;
//import org.apache.commons.lang3.tuple.Pair;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.util.List;
//import java.util.*;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class TestMain {
//    private static final double DISTANCE_THRESHOLD = (20);
//    private static final double STRICT_THRESHOLD = (10);
//
//    private static final double ERROR_MATCHING = (20);
//    private static final double STRICT_ERROR = (10);
//
//    public static void main(String[] args) {
//        String pathname = "C:\\Users\\ishwi\\Pictures\\Downloads";
//        File file = new File(pathname);
//        assert file.isDirectory();
//        if (false) {
//            ConcurrentLastFM newInstance = LastFMFactory.getNewInstance();
//            LinkedBlockingQueue<UrlCapsule> a = new LinkedBlockingQueue<>();
//            try {
//                newInstance.getChart("lukyfan", "overall", 1500, 1, TopEntity.ALBUM, AlbumChart.getAlbumParser(ChartParameters.toListParams()), a);
//            } catch (LastFmException e) {
//                e.printStackTrace();
//            }
//            List<UrlCapsule> b = new ArrayList<>();
//            a.drainTo(b);
//            for (UrlCapsule urlCapsule : b) {
//                try {
//                    URL url = new URL(urlCapsule.getUrl());
//                    BufferedImage read = ImageIO.read(url);
//                    File outputfile = new File(file.getPath() + File.separatorChar + UUID.randomUUID());
//                    ImageIO.write(read, "png", outputfile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        Map<String, Integer> resultMap = new HashMap<>();
//        Map<String, Integer> discardMap = new HashMap<>();
//
//        List<Color> colorList = List.of(Color.white, Color.blue);
//        double[][] weights = new double[][]{
//                {20, 10},
//                {15, 10},
//                {12, 5},
//                {12, 10}};
//        for (int i = 0; i < 4; i++) {
//            for (Color color : colorList) {
//                int counter = 0;
//                int discarded = 0;
//                LinkedBlockingQueue<UrlCapsule> ac = new LinkedBlockingQueue<>();
//                for (File listFile : file.listFiles()) {
//                    try {
//                        BufferedImage read = ImageIO.read(listFile);
//                        Pair<List<Color>, Color> palette = ColorThiefCustom.getPalette(read, 2, 1, false);
//                        Color average = palette.getRight();
//                        List<Color> left = palette.getLeft();
//
//                        int finalI = i;
//                        if ((GraphicUtils.getDistance(color, average) < weights[i][0] ||
//                                left.stream().anyMatch(t -> GraphicUtils.getDistance(color, t) < weights[finalI][0]))) {
//                            if (GraphicUtils.getDistance(average, left.stream()
//                                    .map(z -> Pair.of(color, GraphicUtils.getDistance(color, z)))
//                                    .min(Comparator.comparingDouble(Pair::getRight)).map(Pair::getLeft).orElse(GraphicUtils.getBetter(average)))
//                                    < weights[i][1]) {
//                                counter++;
//                                ac.add(new PreComputedByBrightness(new
//                                        AlbumChart(null, counter, "", "", "", 0, false, false), read, false));
//                            } else {
//                                discarded++;
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                String colors;
//                if (Color.black.equals(color)) {
//                    colors = "BLACK";
//                } else if (Color.blue.equals(color)) {
//                    colors = "blue";
//                } else if (Color.white.equals(color)) {
//                    colors = "white";
//                } else if (Color.pink.equals(color)) {
//                    colors = "pink";
//                } else {
//                    colors = "aaaa";
//                }
//                String ia;
//                switch (i) {
//                    case 0:
//                        ia = "Strict";
//                        break;
//                    case 1:
//                        ia = " Strict search Relaxed delete";
//                        break;
//                    case 2:
//                        ia = " Relaxed search Strict delete";
//                        break;
//                    case 3:
//                        ia = " Relaxed search Relaxed delete";
//
//                        break;
//                    default:
//                        ia = " ";
//                }
//                String format = String.format("resulted %s %s", ia, colors);
//                resultMap.put(format, counter);
//                discardMap.put(String.format(" discarded %s %s", ia, colors), discarded);
//                int x = (int) Math.floor(counter);
//                x = Math.min(5, x);
//                BufferedImage image = CollageMaker.generateCollageThreaded(x, x, ac, ChartQuality.PNG_BIG, false);
//                File outputfile = new File(file.getPath() + File.separatorChar + ".." + File.separatorChar + "results" + File.separatorChar + format + ".png");
//                try {
//                    ImageIO.write(image, "png", outputfile);
//                } catch (IOException e) {
//
//                }
//
//            }
//
//            ;
//
//        }
//        for (Map.Entry<String, Integer> stringIntegerEntry : resultMap.entrySet()) {
//            System.out.println(stringIntegerEntry.getKey());
//            System.out.println(stringIntegerEntry.getValue());
//            System.out.println("\n");
//        }
//        for (Map.Entry<String, Integer> stringIntegerEntry : discardMap.entrySet()) {
//            System.out.println(stringIntegerEntry.getKey());
//            System.out.println(stringIntegerEntry.getValue());
//            System.out.println("\n");
//        }
//    }
//
//}
