package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.TimerFrameParser;
import dao.ChuuService;
import dao.entities.AlbumInfo;
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

public class GenreCommand extends ConcurrentCommand {
    private final MusicBrainzService musicBrainz;

    public GenreCommand(ChuuService dao) {
        super(dao);
        this.parser = new TimerFrameParser(dao, TimeFrameEnum.YEAR);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();
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

        String[] returned = parser.parse(e);
        String username = returned[0];
        long discordId = Long.parseLong(returned[1]);

        String timeframe = returned[2];
        String usableString = getUserString(e, discordId, username);
        List<AlbumInfo> albumInfos = lastFM.getTopAlbums(username, timeframe, 2000).stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                .collect(Collectors.toList());
        Map<Genre, Integer> map = musicBrainz.genreCount(albumInfos);
        if (map.isEmpty()) {
            sendMessageQueue(e, "Was not able to find any genre in  " + usableString + "'s artist");
            return;
        }

        PieChart pieChart =
                new PieChartBuilder()
                        .width(800)
                        .height(600)
                        .title("Top 10 Genres from " + usableString + " in the last " + TimeFrameEnum
                                .fromCompletePeriod(timeframe).toString())
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

        BufferedImage bufferedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        pieChart.paint(g, 800, 600);
        sendImage(bufferedImage, e);

    }


}
