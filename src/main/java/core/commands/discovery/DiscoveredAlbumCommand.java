package core.commands.discovery;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.charts.ChartableCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledAlbum;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DiscoveredAlbumCommand extends ChartableCommand<ChartParameters> {

    public DiscoveredAlbumCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public ChartableParser<ChartParameters> initParser() {
        ChartParser chartParser = new ChartParser(db);
        chartParser.replaceOptional("plays", Optionals.NOPLAYS.opt);
        chartParser.addOptional(new OptionalEntity("plays", "shows this with plays", true, "noplays"));
        return chartParser;
    }

    @Override
    public String getSlashName() {
        return "albums";
    }

    @Override
    public String getDescription() {
        return "Returns a chart with albums discovered";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumdiscovered", "albdiscovered", "discoveredalb", "discoveredalbums");
    }

    @Override
    public String getName() {
        return "Discovered Albums";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        if (param.getTimeFrameEnum().isAllTime()) {
            return new CountWrapper<>(0, queue);
        }
        int i = lastFM.getChart(param.getUser(), param.getTimeFrameEnum(), 3000, 1, TopEntity.ALBUM, ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ALBUM, param, lastFM, param.getUser()),
                queue);
        List<UrlCapsule> capsules = new ArrayList<>(queue.size());
        queue.drainTo(capsules);
        AtomicInteger marker = new AtomicInteger(0);

        Map<ScrobbledAlbum, UrlCapsule> albumToItem = capsules.stream().collect(Collectors.toMap(x -> new ScrobbledAlbum(x.getArtistName(), x.getPlays(), null, -1L, x.getAlbumName(), null),
                x -> x, (x, y) -> {
                    x.setPlays(x.getPlays() + y.getPlays());
                    return x;
                }));
        List<ScrobbledAlbum> discoveredAlbums = db.getDiscoveredAlbums(albumToItem.keySet().stream().toList(), param.getUser().getName());
        marker.set(0);
        queue = albumToItem.entrySet().stream().filter(x -> discoveredAlbums.contains(x.getKey()))
                .map(Map.Entry::getValue).sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                .peek(x -> x.setPos(marker.getAndIncrement())).limit((long) param.getX() * param.getY())
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        return new CountWrapper<>(discoveredAlbums.size(), queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top discovered album", embedBuilder, " has discovered " + count + " albums", params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s discovered albums albums" + time);
        return String.format("%s has discovered %d albums%s (showing top %d)", initTitle, count, time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartParameters parameters) {
        if (parameters.getTimeFrameEnum().isAllTime()) {
            sendMessageQueue(parameters.getE(), "All timeframe is not supported for this command");
            return;
        }
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s hasn't discovered any album%s!", ingo.username(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}


