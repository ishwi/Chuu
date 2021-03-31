package core.apis.last.entities.chartentities;

import core.commands.utils.CommandUtil;
import core.imagerenderer.ChartLine;
import core.parsers.params.ChartParameters;
import dao.entities.NowPlayingArtist;
import dao.utils.LinkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class TrackChart extends UrlCapsule {
    final boolean drawTitles;
    final boolean drawPlays;
    final boolean isAside;

    public TrackChart(String url, int pos, String trackName, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays, boolean isAside) {
        super(url, pos, trackName, artistName, mbid, plays);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
        this.isAside = isAside;
    }

    public TrackChart(String url, int pos, String trackName, String albumName, String mbid, boolean drawTitles, boolean drawPlays, boolean isAside) {
        super(url, pos, trackName, albumName, mbid);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
        this.isAside = isAside;

    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getTrackParser(ChartParameters chartParameters) {
        return (trackObj, size) -> {
            String name = trackObj.getString("name");
            int frequency = trackObj.getInt("playcount");
            String artistName = trackObj.getJSONObject("artist").getString("name");
            JSONArray image = trackObj.getJSONArray("image");
            JSONObject bigImage = image.getJSONObject(image.length() - 1);

            return new TrackChart(bigImage.getString("#text"), size, name, artistName, null, frequency, chartParameters.isWriteTitles(), chartParameters.isWritePlays(), chartParameters.isAside());
        };
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getDailyTrackParser(ChartParameters chartParameters) {
        return (jsonObject, ignored) ->
        {
            NowPlayingArtist x = AlbumChart.fromRecentTrack(jsonObject, TopEntity.TRACK);
            return new TrackChart(x.url(), 0, x.songName(), x.artistName(), x.artistMbid(), 1,
                    chartParameters.isWriteTitles()
                    , chartParameters.isWritePlays(), chartParameters.isAside());
        };
    }


    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getAlbumName(), ChartLine.Type.TITLE));
            list.add(new ChartLine(getArtistName()));
            if (isAside) {
                Collections.reverse(list);
            }
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
                LinkUtils.getLastFMArtistTrack(getArtistName(), getAlbumName()),
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
