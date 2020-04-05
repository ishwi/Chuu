package core.commands;

import core.apis.last.chartentities.ArtistChart;
import core.imagerenderer.GraphicUtils;
import core.parsers.OnlyChartSizeParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartParameters;
import core.parsers.params.GuildParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledArtist;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuildTopCommand extends ChartableCommand {

    public GuildTopCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        this.parser = new OnlyChartSizeParser(
                new OptionalEntity("--global", " show artist from all bot users instead of only from this server"));
        parser.replaceOptional("--plays", new OptionalEntity("--noplays", "don't display plays"));


    }

    @Override
    public String getDescription() {
        return ("Chart of a server most listened artist");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("server", "guild", "general");
    }

    @Override
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new GuildParameters(message, e, Integer.parseInt(message[0]), Integer.parseInt(message[1]));
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) {
        GuildParameters gp = (GuildParameters) params;
        ResultWrapper<ScrobbledArtist> guildTop = getService().getGuildTop(gp.isGlobal() ? null : gp.getE().getGuild().getIdLong(), gp.getX() * gp.getY(), (gp.isList() || gp.isPieFormat()));
        AtomicInteger counter = new AtomicInteger(0);
        BlockingQueue<UrlCapsule> collect = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledArtist::getCount).reversed()).
                map(x ->
                        new ArtistChart(x.getUrl(), counter.getAndIncrement(), x.getArtist(), null, x.getCount(), params.isWriteTitles(), params.isWritePlays())
                ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(guildTop.getRows(), collect);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top artists", embedBuilder, " has listened to " + count + " artists");
    }

    @Override
    public void doPie(PieChart pieChart, ChartParameters params, int count) {
        GuildParameters gp = (GuildParameters) params;
        String urlImage;
        String subtitle;
        if (gp.isGlobal()) {
            subtitle = configPieChart(pieChart, params, count, params.getE().getJDA().getSelfUser().getName());
            urlImage = params.getE().getJDA().getSelfUser().getAvatarUrl();

        } else {
            subtitle = configPieChart(pieChart, params, count, params.getE().getGuild().getName());
            urlImage = params.getE().getGuild().getIconUrl();
        }
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        Font annotationsFont = pieChart.getStyler().getAnnotationsFont();
        pieChart.paint(g, 1000, 750);
        g.setFont(annotationsFont.deriveFont(11.0f));
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(subtitle, g);
        g.drawString(subtitle, 1000 - 10 - (int) stringBounds.getWidth(), 740 - 2);
        GraphicUtils.inserArtistImage(urlImage, g);
        sendImage(bufferedImage, params.getE());
    }


    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s top artists");
        return String.format("%s has listened to %d artists (showing top %d)", initTitle, count, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters params) {
        GuildParameters gp = (GuildParameters) params;
        if (gp.isGlobal()) {
            sendMessageQueue(e, "No one has listened a single artist in the whole bot");
        } else {
            sendMessageQueue(e, "No one has listened a single artist in this server");
        }
    }

    @Override
    public String getName() {
        return "Server Top Artists";
    }

}



