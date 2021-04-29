package core.commands.charts;

import core.apis.last.entities.chartentities.TrackChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.parsers.ChartableParser;
import core.parsers.OnlyChartSizeParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartSizeParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuildTopTracksCommand extends GuildTopCommand {
    public GuildTopTracksCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    public ChartableParser<ChartSizeParameters> initParser() {
        OnlyChartSizeParser onlyChartSizeParser = new OnlyChartSizeParser(db, TimeFrameEnum.ALL,
                new OptionalEntity("global", " shows albums from all bot users instead of only from this server"));
        onlyChartSizeParser.replaceOptional("plays", new OptionalEntity("noplays", "don't display plays"));
        onlyChartSizeParser.addOptional(new OptionalEntity("plays", "shows this with plays", true, "noplays"));
        onlyChartSizeParser.replaceOptional("list", new OptionalEntity("image", "show this with a chart instead of a list "));
        onlyChartSizeParser.addOptional(new OptionalEntity("list", "shows this in list mode", true, Set.of("image", "pie")));
        onlyChartSizeParser.setExpensiveSearch(false);
        onlyChartSizeParser.setAllowUnaothorizedUsers(true);
        return onlyChartSizeParser;
    }


    @Override
    public String getDescription() {
        return ("Chart of a server most listened songs");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("serversongs", "guildtr", "servertr", "guildsongs", "guildtracks");
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters gp) {
        ChartMode effectiveMode = getEffectiveMode(gp);
        ResultWrapper<ScrobbledTrack> guildTop = db.getGuildTrackTop(gp.hasOptional("global") ? null : gp.getE().getGuild().getIdLong(),
                gp.getX() * gp.getY(),
                !(effectiveMode.equals(ChartMode.IMAGE) && gp.chartMode().equals(ChartMode.IMAGE) || gp.chartMode().equals(ChartMode.IMAGE_ASIDE)));
        AtomicInteger counter = new AtomicInteger(0);
        BlockingQueue<UrlCapsule> guildTopQ = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledTrack::getCount).reversed()).
                map(x ->
                        new TrackChart(x.getImageUrl(), counter.getAndIncrement(), x.getName(), x.getArtist(), null, x.getCount(), gp.isWriteTitles(), gp.isWritePlays(), gp.isAside())
                ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(guildTop.getRows(), guildTopQ);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
        String titleInit = "'s top tracks";
        String footerText = " has listened to " + count + " songs";
        String name = params.getE().getGuild().getName();
        return embedBuilder.setAuthor(name + titleInit,
                null, params.getE().getGuild().getIconUrl())
                .setFooter(CommandUtil.markdownLessString(name) + footerText).setColor(CommandUtil.randomColor(params.getE()));
    }


    @Override
    public String configPieChart(PieChart pieChart, ChartSizeParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s top tracks");
        return String.format("%s has listened to %d tracks (showing top %d)", initTitle, count, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartSizeParameters gp) {
        MessageReceivedEvent e = gp.getE();
        if (gp.hasOptional("global")) {
            sendMessageQueue(e, "No one has listened a single track in the whole bot");
        } else {
            sendMessageQueue(e, "No one has listened a single track in this server");
        }
    }

    @Override
    public String getName() {
        return "Server top tracks";
    }
}
