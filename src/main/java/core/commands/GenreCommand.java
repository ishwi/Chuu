package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.DiscordUserDisplay;
import dao.entities.Genre;
import dao.entities.TimeFrameEnum;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenreCommand extends ConcurrentCommand<TimeFrameParameters> {
    private final MusicBrainzService musicBrainz;

    public GenreCommand(ChuuService dao) {
        super(dao);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public Parser<TimeFrameParameters> getParser() {
        return new TimerFrameParser(getService(), TimeFrameEnum.YEAR);
    }

    @Override
    public String getDescription() {
        return "Top 10 genres from an user";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("genre");
    }

    @Override
    public String getName() {
        return "Top Genres";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        TimeFrameParameters returned = parser.parse(e);
        String username = returned.getLastFMData().getName();
        long discordId = returned.getLastFMData().getDiscordId();

        TimeFrameEnum timeframe = returned.getTime();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoNotStripped(e, discordId);
        String usableString = userInfo.getUsername();
        String urlImage = userInfo.getUrlImage();
        List<AlbumInfo> albumInfos = lastFM.getTopAlbums(username, timeframe.toApiFormat(), 2000).stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .collect(Collectors.toList());
        Map<Genre, Integer> map = musicBrainz.genreCount(albumInfos);
        if (map.isEmpty()) {
            sendMessageQueue(e, "Was not able to find any genre in  " + usableString + "'s artist");
            return;
        }

        PieChart pieChart =
                new PieChartBuilder()
                        .width(1000)
                        .height(750)
                        .title("Top 10 Genres from " + usableString + timeframe.getDisplayString())
                        .theme(Styler.ChartTheme.GGPlot2)
                        .build();
        pieChart.getStyler().setLegendVisible(false);
        pieChart.getStyler().setAnnotationDistance(1.15);
        pieChart.getStyler().setPlotContentSize(.7);
        pieChart.getStyler().setCircular(true);
        pieChart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        pieChart.getStyler().setDrawAllAnnotations(true);
        pieChart.getStyler().setStartAngleInDegrees(90);
        pieChart.getStyler().setPlotBackgroundColor(Color.decode("#2c2f33"));
        pieChart.getStyler().setCursorFontColor(Color.white);
        pieChart.getStyler().setAnnotationsFontColor(Color.white);
        pieChart.getStyler().setPlotBorderVisible(false);
        pieChart.getStyler().setChartTitleBoxBackgroundColor(Color.decode("#23272a"));
        pieChart.getStyler().setChartBackgroundColor(Color.decode("#23272a"));
        pieChart.getStyler().setChartFontColor(Color.white);
        map.entrySet().stream().sorted(((o1, o2) -> o2.getValue().compareTo(o1.getValue()))).sequential().limit(10)
                .forEachOrdered(entry -> {
                    Genre genre = entry.getKey();
                    int plays = entry.getValue();
                    pieChart.addSeries(genre.getGenreName(), plays);
                });

        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();

        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);
        GraphicUtils.inserArtistImage(urlImage, g);
        sendImage(bufferedImage, e);

    }


}
