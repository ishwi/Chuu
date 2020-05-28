package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.IPieable;
import core.imagerenderer.util.PieableChart;
import core.otherlisteners.Reactionary;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public abstract class ChartableCommand<T extends ChartParameters> extends ConcurrentCommand<T> {
    public IPieable<UrlCapsule, ChartParameters> pie;

    public ChartableCommand(ChuuService dao) {
        super(dao);
        this.pie = getPie();
        getParser().setExpensiveSearch(true);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CHARTS;
    }

    public abstract ChartableParser<T> getParser();

    ChartMode getEffectiveMode(ChartParameters chartParameters) {
        ChartMode chartMode = chartParameters.chartMode();
        if ((chartMode.equals(ChartMode.LIST) && !chartParameters.isList() && !chartParameters.isPieFormat() && !chartParameters.isAside())
                ||
                (!chartMode.equals(ChartMode.LIST) && chartParameters.isList())) {
            return ChartMode.LIST;
        } else if ((chartMode.equals(ChartMode.PIE) && !chartParameters.isPieFormat() && !chartParameters.isList() && !chartParameters.isAside())
                || (!chartMode.equals(ChartMode.PIE) && chartParameters.isPieFormat())) {
            return ChartMode.PIE;
        } else {
            return ChartMode.IMAGE;
        }
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        T chartParameters = parser.parse(e);
        if (chartParameters == null)
            return;
        CountWrapper<BlockingQueue<UrlCapsule>> countWrapper = processQueue(chartParameters);
        BlockingQueue<UrlCapsule> urlCapsules = countWrapper.getResult();
        if (urlCapsules.isEmpty()) {
            this.noElementsMessage(chartParameters);
            return;
        }
        ChartMode chartMode = getEffectiveMode(chartParameters);
        switch (chartMode) {
            case IMAGE_INFO:
            case IMAGE:
            case IMAGE_ASIDE:
            case IMAGE_ASIDE_INFO:
                doImage(urlCapsules, chartParameters.getX(), chartParameters.getY(), chartParameters, countWrapper.getRows());
                return;
            default:
            case LIST:
                ArrayList<UrlCapsule> liste = new ArrayList<>(urlCapsules.size());
                urlCapsules.drainTo(liste);
                doList(liste, chartParameters, countWrapper.getRows());
                break;
            case PIE:
                liste = new ArrayList<>(urlCapsules.size());
                urlCapsules.drainTo(liste);
                PieChart pieChart = pie.doPie(chartParameters, liste);
                doPie(pieChart, chartParameters, countWrapper.getRows());
                break;
        }
    }


    public abstract CountWrapper<BlockingQueue<UrlCapsule>> processQueue(T params) throws
            LastFmException;

    void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, MessageReceivedEvent e, T params, int size) {
        int chartSize = queue.size();

        ChartQuality chartQuality = ChartQuality.PNG_BIG;
        int minx = (int) Math.ceil((double) chartSize / x);
        if (minx == 1)
            x = chartSize;
        if (e.isFromGuild()) {
            if ((e.isFromGuild() && e.getGuild().getMaxFileSize() == Message.MAX_FILE_SIZE) || !e.isFromGuild()) {
                if (chartSize > 45 && chartSize < 200)
                    chartQuality = ChartQuality.JPEG_BIG;
                else if (chartSize >= 200)
                    chartQuality = ChartQuality.JPEG_SMALL;
            }
        }
        BufferedImage image = CollageMaker
                .generateCollageThreaded(x, minx, queue, chartQuality, params.isAside() || params.chartMode().equals(ChartMode.IMAGE_ASIDE) || params.chartMode().equals(ChartMode.IMAGE_ASIDE_INFO));

        boolean info = params.chartMode().equals(ChartMode.IMAGE_INFO) || params.chartMode().equals(ChartMode.IMAGE_ASIDE_INFO);
        sendImage(image, e, chartQuality, info ? configEmbed(new EmbedBuilder(), params, size) : null);
    }


    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, T parameters, int size) {
        CompletableFuture<Message> future = null;
        MessageReceivedEvent e = parameters.getE();
        if (queue.size() < x * y) {
            x = Math.max((int) Math.ceil(Math.sqrt(queue.size())), 1);
            y = x;
        }
        if (x * y > 100) {
            future = e.getChannel().sendMessage("Going to take a while").submit();
        }
        generateImage(queue, x, y, e, parameters, size);
        CommandUtil.handleConditionalMessage(future);
    }


    public void doList(List<UrlCapsule> urlCapsules, T params, int count) {

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < urlCapsules.size(); i++) {
            a.append(i + 1).append(urlCapsules.get(i).toEmbedDisplay());
        }
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(params.getE(), params.getDiscordId());

        EmbedBuilder embedBuilder = configEmbed(new EmbedBuilder()
                .setDescription(a)
                .setColor(CommandUtil.randomColor())
                .setThumbnail(userInfoConsideringGuildOrNot.getUrlImage()), params, count);
        MessageBuilder mes = new MessageBuilder();
        params.getE().getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(urlCapsules, message1, embedBuilder));
    }

    public void doPie(PieChart pieChart, T chartParameters, int count) {
        DiscordUserDisplay userInfoNotStripped = CommandUtil.getUserInfoNotStripped(chartParameters.getE(), chartParameters.getDiscordId());
        String subtitle = configPieChart(pieChart, chartParameters, count, userInfoNotStripped.getUsername());
        String urlImage = userInfoNotStripped.getUrlImage();
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        Font annotationsFont = pieChart.getStyler().getAnnotationsFont();
        pieChart.paint(g, 1000, 750);
        g.setFont(annotationsFont.deriveFont(11.0f));
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(subtitle, g);
        g.drawString(subtitle, 1000 - 10 - (int) stringBounds.getWidth(), 740 - 2);
        GraphicUtils.inserArtistImage(urlImage, g);
        sendImage(bufferedImage, chartParameters.getE());
    }


    public abstract EmbedBuilder configEmbed(EmbedBuilder embedBuilder, T params, int count);

    public abstract String configPieChart(PieChart pieChart, T params, int count, String initTitle);

    public abstract void noElementsMessage(T parameters);

    public IPieable<UrlCapsule, ChartParameters> getPie() {
        return new PieableChart(this.parser);
    }


}
