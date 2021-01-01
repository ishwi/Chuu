package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.PreComputedByGayness;
import core.apis.last.chartentities.PreComputedChartEntity;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.last.queues.DiscardByQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.ChartableParser;
import core.parsers.GayParser;
import core.parsers.params.GayParams;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.GayType;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GayCommand extends OnlyChartCommand<GayParams> {


    private final DiscogsApi discogsApi;
    private final Spotify spotify;
    private final BiFunction<Map<Color, Integer>, GayParams, Predicate<PreComputedChartEntity>> discardGenerator = (map, perRow) -> preComputedChartEntity ->
    {
        // The Map hold a list of all rainbow values and a counter of the current values that color hold
        // We only consider colours that havent been filled yet
        //We obtain the set of colours of the pallete of the image
        // THe minimun distance of matching images if it were any that was below the threshold
        //Only taking into account those that are below the threshold
        //Cleaning it a bit
        //Sorting needed if we dont repeat images per gay colour
        Optional<Pair<Map.Entry<Color, Integer>, Pair<Color, Double>>> collect = map.entrySet().stream()
                // We only consider colours that havent been filled yet
                .filter(x -> x.getValue() < perRow.getX())
                .map(rainbowColor -> {
                            double strictThreshold;
                            if (perRow.getGayType() == GayType.LGTBQ) {
                                strictThreshold = ColorChartCommand.STRICT_THRESHOLD;
                            } else if (perRow.getGayType() == GayType.BI || perRow.getGayType() == GayType.TRANS) {
                                strictThreshold = ColorChartCommand.STRICT_THRESHOLD - 2;
                            } else {
                                strictThreshold = ColorChartCommand.STRICT_THRESHOLD + 4;
                            }
                            //We obtain the set of colours of the pallete of the image
                            List<Color> dominantColor1 = preComputedChartEntity.getDominantColor();
                            Set<Color> dominantColor = new HashSet<>(dominantColor1 == null || dominantColor1.isEmpty() ? Collections.emptyList() : List.of(dominantColor1.get(0)));
                            Color averageColor = preComputedChartEntity.getAverageColor();
                            if (averageColor != null) {
                                dominantColor.add(averageColor);
                            }
                            // THe minimun distance of matching images if it were any that was below the threshold
                            double finalStrictThreshold = strictThreshold;
                            Optional<Pair<Color, Double>> min = dominantColor.stream().map(color -> Pair.of(rainbowColor.getKey(), GraphicUtils.getDistance(rainbowColor.getKey(), color)))
                                    .filter(x -> x.getRight() < finalStrictThreshold).min(Comparator.comparingDouble(Pair::getRight));
                            return Pair.of(rainbowColor, min);
                        }

                ).filter(x -> x.getRight().isPresent())
                //Only taking into account those that are below the threshold
                .map(x -> {
                    Pair<Color, Double> colorDoublePair = x.getRight().get();
                    return Pair.of(x.getLeft(), colorDoublePair);
                }).min(Comparator.comparingDouble(x -> x.getRight().getRight()));
        if (collect.isPresent()) {
            Pair<Map.Entry<Color, Integer>, Pair<Color, Double>> result = collect.get();
            map.merge(result.getKey().getKey(), 1, Integer::sum);
            assert preComputedChartEntity instanceof PreComputedByGayness;
            ((PreComputedByGayness) preComputedChartEntity).setDecidedCOlor(result.getKey().getKey());
        }
        return collect.isEmpty();

    };

    public GayCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
    }

    @Override
    public ChartableParser<GayParams> initParser() {
        return new GayParser(getService(), TimeFrameEnum.ALL);
    }

    @Override
    public String getDescription() {
        return "Chart with a LGTBQ pallete";
    }

    @Override
    public List<String> getAliases() {
        return List.of("pride", "gay", "flag", "trans", "ace", "lesbian", "nonbinary", "nb");
    }

    @Override
    public String getName() {
        return "Pride Command";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(GayParams params) throws LastFmException {
        String substring = params.getE().getMessage().getContentRaw().substring(1).split("\\s+")[0].toLowerCase();
        switch (substring) {
            case "trans":
                params.setGayType(GayType.TRANS);
                break;
            case "ace":
                params.setGayType(GayType.ACE);
                break;
            case "nonbinary":
            case "nb":
                params.setGayType(GayType.NB);
                break;
            case "lesbian":
                params.setGayType(GayType.LESBIAN);
                break;
        }
        DiscardByQueue queue;
        List<Color> palettes = params.getGayType().getPalettes();
        Map<Color, Integer> gayColours = palettes.stream().collect(Collectors.toMap(x -> x, x -> 0, (x, y) -> x - params.getX()));

        int count;
        Function<UrlCapsule, PreComputedChartEntity> factoryFunction =
                (capsule) ->
                {
                    BufferedImage image = GraphicUtils.getImage(capsule.getUrl());
                    PreComputedChartEntity.ImageComparison comparison = PreComputedChartEntity.ImageComparison.AVERAGE_AND_DOMINANT_PALETTE;
                    return new PreComputedByGayness(capsule, image, true, comparison);
                };

        queue = new DiscardByQueue(getService(), discogsApi, spotify, discardGenerator.apply(gayColours, params), factoryFunction, params.getX() * params.getY());
        if (params.hasOptional("artist")) {
            count = lastFM.getChart(params.getLastfmID(),
                    params.getTimeFrameEnum(),
                    3000,
                    1,
                    TopEntity.ARTIST,
                    ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ARTIST, params, lastFM, params.getLastfmID()),
                    queue);
        } else {
            count = lastFM.getChart(params.getLastfmID(), params.getTimeFrameEnum(), 3000, 1, TopEntity.ALBUM, ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ALBUM, params, lastFM, params.getLastfmID()), queue);
        }

        List<UrlCapsule> holding = new ArrayList<>();
        queue.drainTo(holding);
        OptionalInt min = gayColours.values().stream().mapToInt(x -> x).min();
        if (min.isEmpty() || min.orElse(0) == 0) {
            return new CountWrapper<>(count, new LinkedBlockingDeque<>());
        }
        int minValue = min.getAsInt();
        if (minValue < params.getX()) {
            if ((params.getGayType().equals(GayType.BI) && (gayColours.get(Color.decode("#D60270")) < params.getX() || gayColours.get(Color.decode("#0038A8")) < params.getX()))
                    ||
                    (params.getGayType().equals(GayType.TRANS) && (gayColours.get(Color.decode("#55CDFC")) < params.getX() || gayColours.get(Color.decode("#F7A8B8")) < params.getX())))
                params.setX(params.getX() - (int) Math.ceil((params.getX() - minValue) / 2f));
            else {
                params.setX(minValue);
            }
        }
        int rows = params.getX();
        int cols = params.getY();
        Map<Integer, AtomicInteger> colourCounter = IntStream.range(0, cols + 1).boxed().collect(Collectors.toMap(x -> x, x -> new AtomicInteger(0)));


        LinkedBlockingDeque<UrlCapsule> retunable = holding.stream().map(x -> (PreComputedByGayness) x).sorted().limit(holding.size()).peek(
                x -> {
                    List<Integer> matchingIndices = IntStream.range(0, palettes.size())
                            .filter(t -> x.getDecidedCOlor().equals(palettes.get(t))).boxed()// Only keep those indices
                            .collect(Collectors.toList());
                    boolean matched = false;
                    for (Integer i : matchingIndices) {
                        assert i != -1;
                        AtomicInteger integer = colourCounter.get(i);
                        assert integer != null;

                        int andIncrement = integer.getAndIncrement();
                        if (andIncrement < params.getX()) {
                            x.setPos(rows * (Math.max(0, i)) + andIncrement);
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        x.setPos(Integer.MAX_VALUE);
                    }
                }).
                sorted(Comparator.comparingInt(UrlCapsule::getPos)).limit(rows * cols).collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(count, retunable);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, GayParams params, int count) {

        return params.initEmbed("'s pride chart", embedBuilder, ", Happy Pride Month UwU", params.getLastfmID());

    }


    @Override
    public void noElementsMessage(GayParams parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find enough matching covers for %s's pride chart%s", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));

    }

}
