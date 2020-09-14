package core.commands;

import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ArtistChart;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuildTopAlbumsCommand extends GuildTopCommand {
    public GuildTopAlbumsCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    public ChartableParser<ChartSizeParameters> getParser() {
        OnlyChartSizeParser onlyChartSizeParser = new OnlyChartSizeParser(getService(), TimeFrameEnum.ALL,
                new OptionalEntity("global", " shows albums from all bot users instead of only from this server"));
        onlyChartSizeParser.replaceOptional("plays", new OptionalEntity("noplays", "don't display plays"));
        onlyChartSizeParser.addOptional(new OptionalEntity("plays", "shows this with plays", true, "noplays"));
        onlyChartSizeParser.setAllowUnaothorizedUsers(true);
        return onlyChartSizeParser;
    }


    @Override
    public String getDescription() {
        return ("Chart of a server most listened albums");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("serveralbums", "guildalb", "servera", "generalalb", "serveralb", "serveral", "albserver", "alserver");
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters gp) {
        ChartMode effectiveMode = getEffectiveMode(gp);
        ResultWrapper<ScrobbledAlbum> guildTop = getService().getGuildAlbumTop(gp.hasOptional("global") ? null : gp.getE().getGuild().getIdLong(),
                gp.getX() * gp.getY(),
                !(effectiveMode.equals(ChartMode.IMAGE) && gp.chartMode().equals(ChartMode.IMAGE) || gp.chartMode().equals(ChartMode.IMAGE_ASIDE)));
        AtomicInteger counter = new AtomicInteger(0);
        BlockingQueue<UrlCapsule> collect = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledAlbum::getCount).reversed()).
                map(x ->
                        new AlbumChart(x.getUrl(), counter.getAndIncrement(), x.getAlbum(), x.getArtist(), null, x.getCount(), gp.isWriteTitles(), gp.isWritePlays())
                ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(guildTop.getRows(), collect);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
        String titleInit = "'s top albums";
        String footerText = " has listened to " + count + " albums";
        String name = params.getE().getGuild().getName();
        return embedBuilder.setAuthor(name + titleInit,
                null, params.getE().getGuild().getIconUrl())
                .setFooter(CommandUtil.markdownLessString(name) + footerText).setColor(CommandUtil.randomColor());
    }


    @Override
    public String configPieChart(PieChart pieChart, ChartSizeParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s top albums");
        return String.format("%s has listened to %d albums (showing top %d)", initTitle, count, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartSizeParameters gp) {
        MessageReceivedEvent e = gp.getE();
        if (gp.hasOptional("global")) {
            sendMessageQueue(e, "No one has listened a single album in the whole bot");
        } else {
            sendMessageQueue(e, "No one has listened a single album in this server");
        }
    }

    @Override
    public String getName() {
        return "Server Top Albums";
    }
}