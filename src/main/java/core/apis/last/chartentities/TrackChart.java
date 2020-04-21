package core.apis.last.chartentities;

import core.apis.last.TopEntity;
import core.commands.CommandUtil;
import core.imagerenderer.ChartLine;
import core.parsers.params.ChartParameters;
import dao.entities.NowPlayingArtist;
import dao.entities.UrlCapsule;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class TrackChart extends UrlCapsule {
    final boolean drawTitles;
    final boolean drawPlays;

    public TrackChart(String url, int pos, String trackName, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays) {
        super(url, pos, trackName, artistName, mbid, plays);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
    }

    public TrackChart(String url, int pos, String trackName, String albumName, String mbid, boolean drawTitles, boolean drawPlays) {
        super(url, pos, trackName, albumName, mbid);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getTrackParser(ChartParameters chartParameters) {
        return (trackObj, size) -> {
            String name = trackObj.getString("name");
            int frequency = trackObj.getInt("playcount");
            String artistName = trackObj.getJSONObject("artist").getString("name");
            return new TrackChart(null, size, name, artistName, null, frequency, chartParameters.isWriteTitles(), chartParameters.isWritePlays());
        };
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getDailyTrackParser(ChartParameters chartParameters) {
        return (jsonObject, ignored) ->
        {
            NowPlayingArtist x = AlbumChart.fromRecentTrack(jsonObject, TopEntity.TRACK);
            return new TrackChart(x.getUrl(), 0, x.getSongName(), x.getArtistName(), x.getMbid(), 1,
                    chartParameters.isWriteTitles()
                    , chartParameters.isWritePlays());
        };
    }


    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getAlbumName(), ChartLine.Type.TITLE));
            list.add(new ChartLine(getArtistName()));
        }
        if (drawPlays) {
            list.add(new ChartLine(getPlays() + " " + CommandUtil.singlePlural(getPlays(), "play", "plays")));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%d** %s%n",
                CommandUtil.cleanMarkdownCharacter(getArtistName())
                , CommandUtil.cleanMarkdownCharacter(getAlbumName()),
                CommandUtil.getLastFMArtistTrack(getArtistName(), getAlbumName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public String toChartString() {
        return String.format("%s - %s %d %s", getArtistName(), getAlbumName(), getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public int getChartValue() {
        return getPlays();
    }


}
