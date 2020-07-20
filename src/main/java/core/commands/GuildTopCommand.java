package core.commands;

import core.apis.last.chartentities.ArtistChart;
import core.imagerenderer.GraphicUtils;
import core.otherlisteners.Reactionary;
import core.parsers.ChartableParser;
import core.parsers.OnlyChartSizeParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartSizeParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
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

public class GuildTopCommand extends ChartableCommand<ChartSizeParameters> {

    public GuildTopCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }


    @Override
    public ChartableParser<ChartSizeParameters> getParser() {
        OnlyChartSizeParser onlyChartSizeParser = new OnlyChartSizeParser(getService(), TimeFrameEnum.ALL,
                new OptionalEntity("--global", " shows artist from all bot users instead of only from this server"));
        onlyChartSizeParser.replaceOptional("--plays", new OptionalEntity("--noplays", "don't display plays"));
        onlyChartSizeParser.addOptional(new OptionalEntity("--plays", "shows this with plays", true, "--noplays"));
        onlyChartSizeParser.setAllowUnaothorizedUsers(true);
        return onlyChartSizeParser;
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
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters gp) {
        ChartMode effectiveMode = getEffectiveMode(gp);
        ResultWrapper<ScrobbledArtist> guildTop = getService().getGuildTop(gp.hasOptional("--global") ? null : gp.getE().getGuild().getIdLong(),
                gp.getX() * gp.getY(),
                !(effectiveMode.equals(ChartMode.IMAGE) && gp.chartMode().equals(ChartMode.IMAGE) || gp.chartMode().equals(ChartMode.IMAGE_ASIDE)));
        AtomicInteger counter = new AtomicInteger(0);
        BlockingQueue<UrlCapsule> collect = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledArtist::getCount).reversed()).
                map(x ->
                        new ArtistChart(x.getUrl(), counter.getAndIncrement(), x.getArtist(), null, x.getCount(), gp.isWriteTitles(), gp.isWritePlays())
                ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(guildTop.getRows(), collect);
    }

    @Override
    public void doList(List<UrlCapsule> urlCapsules, ChartSizeParameters params, int count) {

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < urlCapsules.size(); i++) {
            a.append(i + 1).append(urlCapsules.get(i).toEmbedDisplay());
        }
        Guild guild = params.getE().getGuild();
        EmbedBuilder embedBuilder = configEmbed(new EmbedBuilder()
                .setDescription(a)
                .setColor(CommandUtil.randomColor())
                .setThumbnail(guild.getIconUrl()), params, count);
        MessageBuilder mes = new MessageBuilder();
        params.getE().getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(urlCapsules, message1, embedBuilder));
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
        String titleInit = "'s top artists";
        String footerText = " has listened to " + count + " artists";
        String name = params.getE().getGuild().getName();
        return embedBuilder.setAuthor(name + titleInit,
                null, params.getE().getGuild().getIconUrl())
                .setFooter(CommandUtil.markdownLessString(name) + footerText).setColor(CommandUtil.randomColor());
    }

    @Override
    public void doPie(PieChart pieChart, ChartSizeParameters gp, int count) {
        String urlImage;
        String subtitle;
        if (gp.hasOptional("--global")) {
            subtitle = configPieChart(pieChart, gp, count, gp.getE().getJDA().getSelfUser().getName());
            urlImage = gp.getE().getJDA().getSelfUser().getAvatarUrl();

        } else {
            subtitle = configPieChart(pieChart, gp, count, gp.getE().getGuild().getName());
            urlImage = gp.getE().getGuild().getIconUrl();
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
        sendImage(bufferedImage, gp.getE());
    }


    @Override
    public String configPieChart(PieChart pieChart, ChartSizeParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s top artists");
        return String.format("%s has listened to %d artists (showing top %d)", initTitle, count, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartSizeParameters gp) {
        MessageReceivedEvent e = gp.getE();
        if (gp.hasOptional("--global")) {
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



