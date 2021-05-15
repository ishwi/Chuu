package core.commands.charts;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.*;
import core.apis.last.queues.DiscardByQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.ContextMessageReceived;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.ChartableParser;
import core.parsers.ColorChartParser;
import core.parsers.params.ColorChartParams;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.tuple.Pair;
import org.beryx.awt.color.ColorFactory;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ColorChartCommand extends OnlyChartCommand<ColorChartParams> {
    static final double DISTANCE_THRESHOLD = (25);
    static final double STRICT_THRESHOLD = (14);
    private final AtomicInteger maxConcurrency = new AtomicInteger(4);
    private final double ERROR_MATCHING = (16);
    private final double STRICT_ERROR = (6);
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
            diagonalSort(rows, cols, sorting);
        }
    }

    private static void diagonalSort(int m, int n, List<PreComputedChartEntity> sorting) {
        int counter = 0;

        for (int slice = 0; slice < m + n - 1; ++slice) {
            int z1 = slice < m ? 0 : slice - m + 1;
            int z2 = slice < n ? 0 : slice - n + 1;

            for (int j = slice - z2; j >= z1; --j) {

                sorting.get(counter++).setPos(j * (m) + (slice - j));
            }
        }

    }


    @Override
    public ChartableParser<ColorChartParams> initParser() {
        return new ColorChartParser(db, TimeFrameEnum.MONTH);
    }

    @Override
    public String getDescription() {
        return "Your artists/albums which their cover is of a specific colour";
    }

    @Override
    public List<String> getAliases() {
        return List.of("colour", "color");
    }

    @Override
    public String getName() {
        return "Coloured Chart";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ColorChartParams params) throws LastFmException {

        int count;
        Function<UrlCapsule, PreComputedChartEntity> factoryFunction =
                (capsule) ->
                {
                    String cover = Chuu.getCoverService().getCover(capsule.getArtistName(), capsule.getAlbumName(), capsule.getUrl(), params.getE());
                    capsule.setUrl(cover);
                    BufferedImage image = GraphicUtils.getImage(cover);
                    PreComputedChartEntity.ImageComparison comparison = params.getColors().size() > 1 ?
                                                                        PreComputedChartEntity.ImageComparison.AVERAGE_AND_DOMINANT_PALETTE :
                                                                        PreComputedChartEntity.ImageComparison.AVERAGE_AND_DOMINANT_PALETTE;
                    boolean isDarkToWhite = params.isInverse();
                    if (params.isSorted()) {
                        return new PreComputedPlays(capsule, image, isDarkToWhite, comparison);
                    }
                    if (params.isColor()) {
                        return new PreComputedByColor(capsule, image, isDarkToWhite, comparison);
                    }
                    return new PreComputedByBrightness(capsule, image, isDarkToWhite, comparison);
                };

        BlockingQueue<UrlCapsule> queue = new DiscardByQueue(db, discogsApi, spotifyApi, discardGenerator.apply(params), factoryFunction, params.getX() * params.getY());
        if (params.isArtist()) {

            count = lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), 3000, 1, TopEntity.ARTIST, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ARTIST, params, lastFM, params.getUser()), queue);
        } else {
            count = lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), 2700, 1, TopEntity.ALBUM, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ALBUM, params, lastFM, params.getUser()), queue);
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
        List<PreComputedChartEntity> sorting = holding.stream().map(x -> (PreComputedChartEntity) x).sorted().limit((long) rows * cols).toList();

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
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ColorChartParams params, int count) {
        String stringBuilder = "top " +
                               (params.isStrict() ? " strict " : "") +
                               getColorsParams(params) +
                               (params.isArtist() ? " artist " : " albums ") +
                               "sorted by " +
                               (params.isSorted() ? "plays" : params.isColor() ? "color" : "brightness") +
                               (params.isInverse() ? " inversed" : "") +
                               " ordered by " + (params.isColumn() ? "column" : params.isLinear() ? "rows" : "diagonal");
        return params.initEmbed("'s " + stringBuilder, embedBuilder, " has listened to " + count + (params.isArtist() ? " artists" : " albums"), params.getUser().getName());


    }

    @Override
    public void noElementsMessage(ColorChartParams parameters) {

        sendMessageQueue(parameters.getE(), "Couldn't get any image searching by " + getColorsParams(parameters));
    }

    private String getColorsParams(ColorChartParams params) {
        if (params.getE() instanceof ContextMessageReceived mes) {
            Message message = mes.e().getMessage();
            return Arrays.stream(message.getContentRaw().split("\\s+")).filter(x ->
            {
                try {
                    ColorFactory.valueOf(x);
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }).collect(Collectors.joining(", "));
        }
        return "";
    }
}
