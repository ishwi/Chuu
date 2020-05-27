package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.PreComputedByBrightness;
import core.apis.last.chartentities.PreComputedByColor;
import core.apis.last.chartentities.PreComputedChartEntity;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.ChartableParser;
import core.parsers.RainbowParser;
import core.parsers.params.RainbowParams;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// Credits to http://thechurchofkoen.com/ for the idea and allowing me to do this command
public class RainbowChartCommand extends OnlyChartCommand<RainbowParams> {
    private final AtomicInteger maxConcurrency = new AtomicInteger(3);
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;

    public RainbowChartCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();

    }

    @Override
    public ChartableParser<RainbowParams> getParser() {
        return new RainbowParser(getService(), TimeFrameEnum.ALL);

    }

    @Override
    void handleCommand(MessageReceivedEvent e) {
        if (maxConcurrency.decrementAndGet() == 0) {
            sendMessageQueue(e, "There are a lot of people executing this command right now, try again later :(");
            maxConcurrency.incrementAndGet();
        } else {
            try {
                super.handleCommand(e);
            } catch (Throwable ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            } finally {
                maxConcurrency.incrementAndGet();
            }
        }
    }


    @Override
    public String getDescription() {
        return "A artist/album chart shown by colors";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rainbow");
    }

    @Override
    public String getName() {
        return "Rainbow";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(RainbowParams param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue;

        int count;
        if (param.isArtist()) {
            queue = new ArtistQueue(getService(), discogsApi, spotifyApi, true);
            count = lastFM.getChart(param.getLastfmID(), param.getTimeFrameEnum().toApiFormat(), (int) (param.getX() * 1.4),
                    (int) (param.getY() * 1.4), TopEntity.ARTIST, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ARTIST, param, lastFM, param.getLastfmID()), queue);
        } else {
            queue = new ArrayBlockingQueue<>((int) (param.getX() * param.getY() * 1.4 * 1.4));
            count = lastFM.getChart(param.getLastfmID(), param.getTimeFrameEnum().toApiFormat(), (int) (param.getX() * 1.4), (int) (param.getY() * 1.4), TopEntity.ALBUM,
                    ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ALBUM, param, lastFM, param.getLastfmID()), queue);
        }
        boolean inverted = param.isInverse();

        List<UrlCapsule> temp = new ArrayList<>();
        queue.drainTo(temp);
        AtomicInteger coutner = new AtomicInteger(0);
        temp = temp.stream().filter(x -> !x.getUrl().isBlank()).takeWhile(x -> coutner.incrementAndGet() <= param.getX() * param.getY()).collect(Collectors.toList());
        int rows = param.getX();
        int cols = param.getY();

        if (temp.size() < rows * cols || rows != cols) {
            rows = (int) Math.floor(Math.sqrt(temp.size()));
            cols = rows;
            param.setX(rows);
            param.setY(cols);

        }
        List<PreComputedChartEntity> collect = temp.parallelStream().map(x -> {
            BufferedImage image = GraphicUtils.getImage(x.getUrl());
            if (param.isColor()) {
                return new PreComputedByColor(x, image, inverted);
            } else {
                return new PreComputedByBrightness(x, image, inverted);
            }
        }).sorted().limit(rows * cols).collect(Collectors.toList());
        if (param.isColumn()) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    PreComputedChartEntity preComputed = collect.get(i * rows + j);
                    preComputed.setPos(j * cols + i);
                }
            }
        } else ColorChartCommand.diagonalSort(rows, cols, collect, param.isLinear());
        queue = new LinkedBlockingQueue<>(cols * rows);
        queue.addAll(collect);
        return new CountWrapper<>(count, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, RainbowParams params, int count) {
        StringBuilder stringBuilder = new StringBuilder("top ").append(params.getX() * params.getY()).append(params.isArtist() ? " artist " : "albums ");
        stringBuilder.append(params.isColor() ? "by color" : "by brightness")
                .append(params.isInverse() ? " inversed" : "")
                .append(" ordered by ").append(params.isColumn() ? "column" : params.isLinear() ? "rows" : "diagonal");
        return params.initEmbed("'s " + stringBuilder.toString(), embedBuilder, " has listened to " + count + (params.isArtist() ? " artists" : " albums"), params.getLastfmID());

    }


    @Override
    public void noElementsMessage(RainbowParams parameters) {
        String s = parameters.isArtist() ? "artists" : "albums";
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any %s%s!", ingo.getUsername(), s, parameters.getTimeFrameEnum().getDisplayString()));

    }
}
