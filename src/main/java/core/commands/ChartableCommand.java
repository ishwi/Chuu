package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartLine;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.imagerenderer.GraphicUtils;
import core.otherlisteners.Reactionary;
import core.parsers.ChartParser;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class ChartableCommand extends ConcurrentCommand {

    public ChartableCommand(ChuuService dao) {
        super(dao);
        this.parser = new ChartParser(getService());
    }

    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        return new ChartParameters(message, e);
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;
        ChartParameters chartParameters = getParameters(returned, e);
        CountWrapper<BlockingQueue<UrlCapsule>> countWrapper = processQueue(chartParameters);
        BlockingQueue<UrlCapsule> urlCapsules = countWrapper.getResult();
        if (urlCapsules.isEmpty()) {
            this.noElementsMessage(e, chartParameters);
            return;
        }
        if (chartParameters.isList() || chartParameters.isPieFormat()) {
            ArrayList<UrlCapsule> liste = new ArrayList<>(urlCapsules.size());
            urlCapsules.drainTo(liste);
            if (chartParameters.isPieFormat()) {
                doPie(liste, chartParameters, countWrapper.getRows());
            } else {
                doList(liste, chartParameters, countWrapper.getRows());
            }
        } else {
            doImage(urlCapsules, chartParameters.getX(), chartParameters.getY(), chartParameters);
        }
    }


    public abstract CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException;

    void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, MessageReceivedEvent e) {
        int size = queue.size();
        ChartQuality chartQuality = ChartQuality.PNG_BIG;
        int minx = (int) Math.ceil((double) size / x);
        //int miny = (int) Math.ceil((double) size / y);
        if (minx == 1)
            x = size;
        if (size > 45 && size < 400)
            chartQuality = ChartQuality.JPEG_BIG;
        else if (size >= 400)
            chartQuality = ChartQuality.JPEG_SMALL;
        BufferedImage image = CollageMaker
                .generateCollageThreaded(x, minx, queue, chartQuality);
        sendImage(image, e, chartQuality);
    }


    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, ChartParameters parameters) {
        CompletableFuture<Message> future = null;
        MessageReceivedEvent e = parameters.getE();
        if (x * y > 100) {
            future = e.getChannel().sendMessage("Going to take a while").submit();
        }
        generateImage(queue, x, y, e);
        CommandUtil.handleConditionalMessage(future);
    }


    public void doList(List<UrlCapsule> urlCapsules, ChartParameters params, int count) {

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
                executor.execute(() -> new Reactionary<>(urlCapsules, message1, embedBuilder)));
    }

    public void doPie(List<UrlCapsule> urlCapsules, ChartParameters chartParameters, int count) {


        PieChart pieChart =
                new PieChartBuilder()
                        .width(1000)
                        .height(750).theme(Styler.ChartTheme.GGPlot2)
                        .build();

        PieStyler styler = pieChart.getStyler();
        styler.setLegendVisible(false);
        styler.setAnnotationDistance(1.15);
        styler.setPlotContentSize(.7);
        styler.setCircular(true);
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        styler.setDrawAllAnnotations(true);
        styler.setStartAngleInDegrees(0);
        styler.setPlotBackgroundColor(Color.decode("#2c2f33"));
        styler.setCursorFontColor(Color.white);
        styler.setAnnotationsFontColor(Color.white);
        styler.setToolTipsAlwaysVisible(true);
        styler.setPlotBorderVisible(false);
        styler.setChartTitleBoxBackgroundColor(Color.decode("#23272a"));
        styler.setChartBackgroundColor(Color.decode("#23272a"));
        styler.setChartFontColor(Color.white);
        styler.getDefaultSeriesRenderStyle();
        styler.setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        int total = urlCapsules.stream().mapToInt(UrlCapsule::getChartValue).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        Map<Boolean, List<Map.Entry<UrlCapsule, Integer>>> parted = new HashMap<>(2);
        parted.put(true, new ArrayList<>());
        parted.put(false, new ArrayList<>());
        var entries = parted.get(true);
        urlCapsules.forEach(x -> {
            if (x.getPos() < 10 || (counter.get() < breakpoint && entries.size() < 15)) {
                entries.add(Map.entry(x, x.getChartValue()));
                counter.addAndGet(x.getChartValue());
            } else {
                parted.get(false).add(Map.entry(x, x.getChartValue()));
            }
        });
        UrlCapsule probe = urlCapsules.get(0);
        if (probe.getLines().isEmpty()) {
            styler.setAnnotationType(PieStyler.AnnotationType.Percentage);
        }

        int sum = parted.get(false).stream().mapToInt(Map.Entry::getValue).sum();
        counter.set(0);
        entries.forEach(entry -> {
            String collect = entry.getKey().getLines().stream().map(ChartLine::getLine).collect(Collectors.joining(" - "));
            int i = counter.incrementAndGet();
            try {
                pieChart.addSeries(collect.isBlank() ? UUID.randomUUID().toString() : collect, entry.getValue());
            } catch (IllegalArgumentException ex) {
                pieChart.addSeries("\u200B".repeat(i) + collect, entry.getValue());
            }
        });

        if (sum != 0) {
            //To avoid having an artist called others and colliding bc no duplicates allowed
            pieChart.addSeries("Others\u200B", sum);
        }
        DiscordUserDisplay userInfoNotStripped = CommandUtil.getUserInfoNotStripped(chartParameters.getE(), chartParameters.getDiscordId());
        String subtitle = configPieChart(pieChart, chartParameters, count, userInfoNotStripped.getUsername());
        String urlImage = userInfoNotStripped.getUrlImage();
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        Font annotationsFont = pieChart.getStyler().getAnnotationsFont();

        pieChart.paint(g, 1000, 750);
        g.setFont(annotationsFont.deriveFont(11.0f));
        int i = g.getFontMetrics().stringWidth(subtitle);
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(subtitle, g);

        g.drawString(subtitle, 1000 - 10 - (int) stringBounds.getWidth(), 740 - 2);
        GraphicUtils.inserArtistImage(urlImage, g);
        sendImage(bufferedImage, chartParameters.getE());

    }

    public abstract EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count);

    public abstract String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle);


    public abstract void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters);


}
