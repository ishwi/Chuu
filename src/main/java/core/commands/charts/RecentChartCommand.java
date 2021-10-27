package core.commands.charts;

import core.apis.last.entities.chartentities.TrackChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.OnlyChartSizeParser;
import core.parsers.params.ChartSizeParameters;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RecentChartCommand extends ChartableCommand<ChartSizeParameters> {

    public RecentChartCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    public ChartableParser<ChartSizeParameters> initParser() {
        return new OnlyChartSizeParser(db);
    }

    @Override
    public String getSlashName() {
        return "recent";
    }

    @Override
    public String getDescription() {
        return "Chart with recents";
    }

    @Override
    public List<String> getAliases() {
        return List.of("recentchart", "rc");
    }

    @Override
    public String getName() {
        return "Recent chart";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters param) throws LastFmException {
        AtomicInteger ranker = new AtomicInteger();
        BlockingQueue<UrlCapsule> queue = lastFM.getRecent(param.getUser(), param.getX() * param.getY()).stream()
                .map(w -> {
                    TrackChart tc = new TrackChart(w.url(), ranker.getAndIncrement(), w.songName(), w.albumName(), null, param.isWriteTitles(), param.isWritePlays(), param.isAside());
                    tc.setPlays(1);
                    return tc;
                })
                .collect(Collectors.toCollection(() -> new ArrayBlockingQueue<>(param.getX() * param.getY())));
        return new CountWrapper<>(ranker.get(), queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
        String handleCount = "'s " + count + " recent tracks";

        return params.initEmbed("'s recent tracks", embedBuilder, handleCount, params.getUser().getName())
                .setFooter("Showing last %d %s".formatted(count, CommandUtil.singlePlural(count, "song", "songs")));
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartSizeParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s recent songs");
        return String.format("Last %d %s", count, CommandUtil.singlePlural(count, "song", "songs"));
    }


    @Override
    public void noElementsMessage(ChartSizeParameters parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoEscaped(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any song!", ingo.username()));
    }


}
