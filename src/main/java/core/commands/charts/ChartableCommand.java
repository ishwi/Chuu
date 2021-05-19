package core.commands.charts;

import core.Chuu;
import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.TrackDurationAlbumArtistChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.CollageMaker;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.PieableListChart;
import core.otherlisteners.Reactionary;
import core.parsers.ChartableParser;
import core.parsers.DaoParser;
import core.parsers.params.ChartParameters;
import dao.ServiceView;
import dao.entities.ChartMode;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledAlbum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public abstract class ChartableCommand<T extends ChartParameters> extends ConcurrentCommand<T> {
    public final IPieableList<UrlCapsule, ChartParameters> pie;

    public ChartableCommand(ServiceView dao) {
        super(dao);
        this.pie = getPie();
        ((DaoParser<?>) getParser()).setExpensiveSearch(true);
        order = 3;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CHARTS;
    }

    public abstract ChartableParser<T> initParser();

    ChartMode getEffectiveMode(ChartParameters chartParameters) {
        if (chartParameters.isList()) {
            return ChartMode.LIST;
        } else if (chartParameters.isPie()) {
            return ChartMode.PIE;
        }
        return ChartMode.IMAGE;
    }

    @Override
    protected void onCommand(Context e, @NotNull T params) throws LastFmException {

        CountWrapper<BlockingQueue<UrlCapsule>> countWrapper = processQueue(params);
        BlockingQueue<UrlCapsule> urlCapsules = countWrapper.getResult();
        if (urlCapsules.isEmpty()) {
            this.noElementsMessage(params);
            return;
        }
        ChartMode chartMode = getEffectiveMode(params);
        switch (chartMode) {
            case IMAGE_INFO:
            case IMAGE:
            case IMAGE_ASIDE:
            case IMAGE_ASIDE_INFO:
                doImage(urlCapsules, params.getX(), params.getY(), params, countWrapper.getRows());
                return;
            default:
            case LIST:
                ArrayList<UrlCapsule> liste = new ArrayList<>(urlCapsules.size());
                urlCapsules.drainTo(liste);
                doList(liste, params, countWrapper.getRows());
                break;
            case PIE:
                liste = new ArrayList<>(urlCapsules.size());
                urlCapsules.drainTo(liste);
                PieChart pieChart = pie.doPie(params, liste);
                doPie(pieChart, params, countWrapper.getRows());
                break;
        }
    }


    public abstract CountWrapper<BlockingQueue<UrlCapsule>> processQueue(T params) throws
            LastFmException;

    void generateImage(BlockingQueue<UrlCapsule> queue, int x, int y, Context e, T params, int size) {
        int chartSize = queue.size();

        ChartQuality chartQuality = ChartQuality.PNG_BIG;
        int minx = (int) Math.ceil((double) chartSize / x);
        if (minx == 1)
            x = chartSize;
        if ((e.isFromGuild() && e.getGuild().getMaxFileSize() == Message.MAX_FILE_SIZE) || !e.isFromGuild()) {
            if (chartSize > 45 && chartSize < 200)
                chartQuality = ChartQuality.JPEG_BIG;
            else if (chartSize >= 200)
                chartQuality = ChartQuality.JPEG_SMALL;
        } else {
            if (e.getGuild().getMaxFileSize() == (50 << 20)) {
                if (chartSize > 1000) {
                    chartQuality = ChartQuality.JPEG_BIG;
                }
            } else if (chartSize > 4000) {
                chartQuality = ChartQuality.JPEG_BIG;
            }
        }
        BufferedImage image = CollageMaker
                .generateCollageThreaded(x, minx, queue, chartQuality, params.isAside() || params.chartMode().equals(ChartMode.IMAGE_ASIDE) || params.chartMode().equals(ChartMode.IMAGE_ASIDE_INFO));

        boolean info = params.chartMode().equals(ChartMode.IMAGE_INFO) || params.chartMode().equals(ChartMode.IMAGE_ASIDE_INFO);
        sendImage(image, e, chartQuality, info ? configEmbed(new ChuuEmbedBuilder(e), params, size) : null);
    }


    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, T parameters, int size) {
        CompletableFuture<Message> future = null;
        Context e = parameters.getE();
        if (queue.size() < x * y) {
            x = Math.max((int) Math.ceil(Math.sqrt(queue.size())), 1);
            //noinspection SuspiciousNameCombination
            y = x;
        }
        if (x * y > 100) {
            future = e.sendMessage("Going to take a while").submit();
        }
        UrlCapsule first = queue.peek();
        if (first instanceof AlbumChart || first instanceof TrackDurationAlbumArtistChart) {
            queue.forEach(t -> t.setUrl(Chuu.getCoverService().getCover(t.getArtistName(), t.getAlbumName(), t.getUrl(), e)));
            if (CommandUtil.rand.nextFloat() >= 0.7f && first instanceof AlbumChart) {
                List<UrlCapsule> items = new ArrayList<>(queue);
                CompletableFuture.runAsync(() -> items.stream()
                        .filter(t -> t.getUrl() != null && !t.getUrl().isBlank())
                        .map(t -> (AlbumChart) t)
                        .forEach(z -> {
                            try {
                                ScrobbledAlbum scrobbledAlbum = CommandUtil.validateAlbum(db, z.getArtistName(), z.getAlbumName(), lastFM, null, null, false, false);
                                db.updateAlbumImage(scrobbledAlbum.getAlbumId(), z.getUrl());
                            } catch (LastFmException ignored) {
                            }
                        }), executor);
            }
        }

        generateImage(queue, x, y, e, parameters, size);
        // Store some album covers

        CommandUtil.handleConditionalMessage(future);
    }


    public void doList(List<UrlCapsule> urlCapsules, T params, int count) {

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < urlCapsules.size(); i++) {
            a.append(i + 1).append(urlCapsules.get(i).toEmbedDisplay());
        }
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(params.getE(), params.getDiscordId());

        EmbedBuilder embedBuilder = configEmbed(new ChuuEmbedBuilder(params.getE())
                .setDescription(a)
                .setThumbnail(userInfoConsideringGuildOrNot.getUrlImage()), params, count);
        params.getE().sendMessage(embedBuilder.build()).queue(message1 ->
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

    public IPieableList<UrlCapsule, ChartParameters> getPie() {
        return new PieableListChart(this.parser);
    }


}
