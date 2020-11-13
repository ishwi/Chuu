package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.exceptions.LastFmException;
import core.parsers.ChartParser;
import core.parsers.ChartableParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DiscoveredArtistCommand extends ChartableCommand<ChartParameters> {

    public DiscoveredArtistCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public ChartableParser<ChartParameters> initParser() {
        ChartParser chartParser = new ChartParser(getService());
        chartParser.replaceOptional("plays", new OptionalEntity("noplays", "don't display plays"));
        chartParser.addOptional(new OptionalEntity("plays", "shows this with plays", true, "noplays"));
        return chartParser;
    }

    @Override
    public String getDescription() {
        return "Returns a chart with artists discovered";
    }

    @Override
    public List<String> getAliases() {
        return List.of("discovered");
    }

    @Override
    public String getName() {
        return "Discovered Artists";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();

        if (param.getTimeFrameEnum().isAllTime()) {
            return new CountWrapper<>(0, queue);
        }
        int i = lastFM.getChart(param.getLastfmID(), param.getTimeFrameEnum(), 3000, 1, TopEntity.ARTIST, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ARTIST, param, lastFM, param.getLastfmID()),
                queue);
        List<UrlCapsule> capsules = new ArrayList<>(queue.size());
        queue.drainTo(capsules);
        AtomicInteger marker = new AtomicInteger(0);

        Map<ScrobbledArtist, UrlCapsule> collect = capsules.stream().collect(Collectors.toMap(x -> new ScrobbledArtist(x.getArtistName(), x.getPlays(), null),
                x -> x, (x, y) -> {
                    x.setPlays(x.getPlays() + y.getPlays());
                    return x;
                }));
        List<ScrobbledArtist> discoveredArtists = getService().getDiscoveredArtists(collect.keySet(), param.getLastfmID());
        marker.set(0);
        queue = collect.entrySet().stream().filter(x -> {
            boolean contains = discoveredArtists.contains(x.getKey());
            if (contains) {
                x.getValue().setUrl(x.getKey().getUrl());
            }
            return contains;
        })
                .map(Map.Entry::getValue).sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                .peek(x -> x.setPos(marker.getAndIncrement())).limit(param.getX() * param.getY())
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        return new CountWrapper<>(discoveredArtists.size(), queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top discovered artists", embedBuilder, " has discovered " + count + " artists", params.getLastfmID());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s discovered artists" + time);
        return String.format("%s has discovered %d artists%s (showing top %d)", initTitle, count, time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartParameters parameters) {
        if (parameters.getTimeFrameEnum().isAllTime()) {
            sendMessageQueue(parameters.getE(), "All timeframe is not supported for this command");
            return;

        }
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s hasn't discovered any artist%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}


