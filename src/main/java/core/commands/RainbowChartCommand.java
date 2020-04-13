package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.*;
import core.apis.last.queues.ArtistQueue;
import core.exceptions.LastFmException;
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
import org.knowm.xchart.PieChart;

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

public class RainbowChartCommand extends ArtistAbleCommand<RainbowParams> {
    private final AtomicInteger maxConcurrency = new AtomicInteger(3);

    public RainbowChartCommand(ChuuService dao) {
        super(dao);
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
            super.handleCommand(e);
            maxConcurrency.incrementAndGet();
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
            count = lastFM.getChart(param.getLastfmID(), param.getTimeFrameEnum().toApiFormat(), param.getX(), param.getY(), TopEntity.ARTIST, ArtistChart.getArtistParser(param), queue);
        } else {
            queue = new ArrayBlockingQueue<>(param.getX() * param.getY());
            count = lastFM.getChart(param.getLastfmID(), param.getTimeFrameEnum().toApiFormat(), param.getX(), param.getY(), TopEntity.ALBUM, AlbumChart.getAlbumParser(param), queue);
        }
        boolean inverted = param.isInverse();

        List<UrlCapsule> temp = new ArrayList<>();
        queue.drainTo(temp);
        int rows = param.getX();
        int cols = param.getY();

        if (temp.size() < rows * cols || rows != cols) {
            rows = (int) Math.floor(Math.sqrt(temp.size()));
            cols = rows;
            param.setX(rows);
            param.setY(cols);

        }
        List<PreComputedChartEntity> collect = temp.parallelStream().map(x -> {
            BufferedImage image;

            try {
                URL url = new URL(x.getUrl());
                image = ImageIO.read(url);

            } catch (IOException | ArrayIndexOutOfBoundsException ex) {
                // https://bugs.openjdk.java.net/browse/JDK-7132728
                image = null;
            }
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
        } else if (param.isLinear()) {
            int t = 0;
            for (PreComputedChartEntity preComputedChartEntity : collect) {
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
                    collect.get(counter++).setPos(i * rows + j);
                    i--;
                    j++;
                }
            }
            for (int k = 1; k < cols; k++) {
                i = cols - 1;
                j = k;
                while (j <= cols - 1) {
                    collect.get(counter++).setPos(i * rows + j);
                    i--;
                    j++;
                }
            }
        }
        queue = new LinkedBlockingQueue<>(cols * rows);
        queue.addAll(collect);
        return new CountWrapper<>(count, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, RainbowParams params, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String configPieChart(PieChart pieChart, RainbowParams params, int count, String initTitle) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void noElementsMessage(RainbowParams parameters) {
        String s = parameters.isArtist() ? "artists" : "albums";
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any %s%s!", ingo.getUsername(), s, parameters.getTimeFrameEnum().getDisplayString()));

    }
}
