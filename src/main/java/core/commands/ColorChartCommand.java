package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.*;
import core.apis.last.queues.DiscardableQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.ChartableParser;
import core.parsers.ColorChartParser;
import core.parsers.params.ColorChartParams;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ColorChartCommand extends OnlyChartCommand<ColorChartParams> {
    private final double DISTANCE_THRESHOLD = (4.0e-7);
    private final double STRICT_THRESHOLD = (8.0e-8);

    private final double ERROR_MATCHING = (9.0e-7);
    private final double STRICT_ERROR = (2.0e-7);
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;

    // xD
    private final Function<ColorChartParams, Predicate<PreComputedChartEntity>> discardGenerator = params -> preComputedChartEntity ->
            params.getColors().stream().noneMatch(color -> {
                double threshold = params.isStrict() ? STRICT_THRESHOLD : DISTANCE_THRESHOLD;
                return preComputedChartEntity.getImage() != null &&
                       (GraphicUtils.getDistance(color, preComputedChartEntity.getAverageColor()) < threshold ||
                        preComputedChartEntity.getDominantColor().stream().anyMatch(palette -> GraphicUtils.getDistance(color, palette) < threshold))
                       &&
                       GraphicUtils.getDistance(preComputedChartEntity.getAverageColor(), preComputedChartEntity.getDominantColor().stream()
                               .map(palette -> Pair.of(color, GraphicUtils.getDistance(color, palette)))
                               .min(Comparator.comparingDouble(Pair::getRight)).map(Pair::getLeft).orElse(GraphicUtils.getBetter(preComputedChartEntity.getAverageColor())))
                       < (params.isStrict() ? STRICT_ERROR : ERROR_MATCHING);
            });

    public ColorChartCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    public static void diagonalSort(int rows, int cols, List<PreComputedChartEntity> sorting, boolean linear) {
        if (linear) {
            int t = 0;
            for (PreComputedChartEntity preComputedChartEntity : sorting) {
                preComputedChartEntity.setPos(t++);
            }
        } else {
            int j;
            int i;
            int counter = 0;
            for (int k = 0; k < rows; k++) {
                i = k;
                j = 0;
                while (i >= 0 && j < cols) {
                    sorting.get(counter++).setPos(i * rows + j);
                    i--;
                    j++;
                }
            }
            for (int k = 1; k < cols; k++) {
                i = cols - 1;
                j = k;
                while (j <= cols - 1) {
                    sorting.get(counter++).setPos(i * rows + j);
                    i--;
                    j++;
                }
            }
        }
    }

    @Override
    public ChartableParser<ColorChartParams> getParser() {
        return new ColorChartParser(getService(), TimeFrameEnum.ALL);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return List.of("color");
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ColorChartParams params) throws LastFmException {

        BlockingQueue<UrlCapsule> queue;
        int count;
        Function<UrlCapsule, PreComputedChartEntity> factoryFunction =
                (capsule) ->
                {
                    BufferedImage image;
                    try {
                        URL url = new URL(capsule.getUrl());
                        image = ImageIO.read(url);
                    } catch (IOException | ArrayIndexOutOfBoundsException ex) {
                        // https://bugs.openjdk.java.net/browse/JDK-7132728
                        image = null;
                    }
                    PreComputedChartEntity.ImageComparison comparison = params.getColors().size() > 1 ?
                            PreComputedChartEntity.ImageComparison.AVERAGE_AND_DOMINANT_PALETTE :
                            PreComputedChartEntity.ImageComparison.AVERAGE_AND_DOMINANT;
                    boolean isDarkToWhite = params.isInverse();
                    if (params.isSorted()) {
                        return new PreComputedPlays(capsule, image, isDarkToWhite, comparison);
                    }
                    if (params.isColor()) {
                        return new PreComputedPlays(capsule, image, isDarkToWhite, comparison);
                    }
                    return new PreComputedByBrightness(capsule, image, isDarkToWhite, comparison);
                };

        queue = new DiscardableQueue(getService(), discogsApi, spotifyApi, discardGenerator.apply(params), factoryFunction, params.getX() * params.getY());
        if (params.isArtist()) {
            count = lastFM.getChart(params.getLastfmID(), params.getTimeFrameEnum().toApiFormat(), 1500, 1, TopEntity.ARTIST, ArtistChart.getArtistParser(params), queue);
        } else {
            count = lastFM.getChart(params.getLastfmID(), params.getTimeFrameEnum().toApiFormat(), 1500, 1, TopEntity.ALBUM, AlbumChart.getAlbumParser(params), queue);
        }

        List<UrlCapsule> holding = new ArrayList<>();
        queue.drainTo(holding);
        if (holding.size() < params.getY() * params.getX()) {
            int ceil = (int) Math.floor(Math.sqrt(holding.size()));
            params.setX(ceil);
            params.setY(ceil);
        }
        int rows = params.getX();
        int cols = params.getY();
        List<PreComputedChartEntity> sorting = holding.stream().map(x -> (PreComputedChartEntity) x).sorted().limit(rows * cols).collect(Collectors.toList());

        if (params.isSorted()) {
            int counter = 0;
            for (PreComputedChartEntity preComputedChartEntity : sorting) {
                preComputedChartEntity.setPos(counter++);
            }
        }
        if (params.isColumn() || (!params.isLinear() && !params.isSorted() && (rows * cols) < holding.size())) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    PreComputedChartEntity preComputed = sorting.get(i * rows + j);
                    preComputed.setPos(j * cols + i);
                }
            }
        } else {
            diagonalSort(rows, cols, sorting, params.isLinear());
        }

        LinkedBlockingDeque<UrlCapsule> retunable = new LinkedBlockingDeque<>(sorting);
        return new CountWrapper<>(count, retunable);
    }

    @Override
    public void noElementsMessage(ColorChartParams parameters) {
        sendMessageQueue(parameters.getE(), "Nuthing");
    }
}
