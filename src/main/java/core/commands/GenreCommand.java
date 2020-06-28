package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GenreCommand extends ConcurrentCommand<NumberParameters<TimeFrameParameters>> {
    private final MusicBrainzService musicBrainz;

    public GenreCommand(ChuuService dao) {
        super(dao);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<TimeFrameParameters>> getParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and a big number");
        String s = "You can also introduce a number to vary the number of genres shown in the pie," +
                "defaults to 10";

        TimerFrameParser timerFrameParser = new TimerFrameParser(getService(), TimeFrameEnum.YEAR);
        timerFrameParser.addOptional(new OptionalEntity("--artist", "use artists instead of albums for the genres"));

        NumberParser<TimeFrameParameters, TimerFrameParser> timeFrameParametersTimerFrameParserNumberParser = new NumberParser<>(timerFrameParser,
                10L,
                Integer.MAX_VALUE,
                map, s, false, true, false);
        return timeFrameParametersTimerFrameParserNumberParser;
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
        NumberParameters<TimeFrameParameters> parse = parser.parse(e);
        TimeFrameParameters returned = parse.getInnerParams();
        String username = returned.getLastFMData().getName();
        long discordId = returned.getLastFMData().getDiscordId();

        TimeFrameEnum timeframe = returned.getTime();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoNotStripped(e, discordId);
        String usableString = userInfo.getUsername();
        String urlImage = userInfo.getUrlImage();
        Map<Genre, Integer> map;
        if (parse.hasOptional("--artist")) {
            List<ArtistInfo> albumInfos = lastFM.getTopArtists(username, timeframe.toApiFormat(), 3000).stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                    .collect(Collectors.toList());
            if (albumInfos.isEmpty()) {
                sendMessageQueue(e, "Was not able to find any genre in " + usableString + "'s top 3000 artists" + returned.getTime().getDisplayString() + " on Musicbrainz");
                return;
            }
            map = musicBrainz.genreCountByartist(albumInfos);
        } else {
            List<AlbumInfo> albumInfos = lastFM.getTopAlbums(username, timeframe.toApiFormat(), 3000).stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                    .collect(Collectors.toList());
            if (albumInfos.isEmpty()) {
                sendMessageQueue(e, "Was not able to find any genre in " + usableString + "'s top 3000 albums" + returned.getTime().getDisplayString() + " on Musicbrainz");
                return;
            }
            map = musicBrainz.genreCount(albumInfos);
        }
        if (map.isEmpty()) {
            sendMessageQueue(e, "Was not able to find any genre in " + usableString + "'s top 3000 " + (parse.hasOptional("--artist") ? "artists" : "albums") + returned.getTime().getDisplayString() + " on Musicbrainz");
            return;
        }

        Long extraParam = parse.getExtraParam();
        PieChart pieChart =
                new PieChartBuilder()
                        .width(1000)
                        .height(750)
                        .title("Top " + extraParam + " Genres from " + usableString + timeframe.getDisplayString())
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
        map.entrySet().stream().sorted(((o1, o2) -> o2.getValue().compareTo(o1.getValue()))).sequential().limit(extraParam)
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
