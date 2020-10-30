package core.apis.last.chartentities;

import core.apis.last.TopEntity;
import core.commands.CommandUtil;
import core.imagerenderer.ChartLine;
import core.parsers.params.ChartGroupParameters;
import dao.entities.NowPlayingArtist;
import dao.entities.Track;
import dao.utils.LinkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class TrackDurationChart extends TrackChart {

    final boolean showDuration;
    int seconds;

    public TrackDurationChart(String url, int pos, String trackName, String artistName, String mbid, int plays, int seconds, boolean drawTitles, boolean drawPlays, boolean showDuration) {
        super(url, pos, trackName, artistName, mbid, plays, drawTitles, drawPlays);
        this.showDuration = showDuration;
        this.seconds = seconds;
    }

    public TrackDurationChart(String url, int pos, String trackName, String albumName, String mbid, boolean drawTitles, boolean drawPlays, boolean showDuration, int seconds) {
        super(url, pos, trackName, albumName, mbid, drawTitles, drawPlays);
        this.showDuration = showDuration;
        this.seconds = seconds;
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getTrackDurationParser(ChartGroupParameters params) {
        return (trackObj, size) -> {
            int duration = trackObj.getInt("duration");
            String name = trackObj.getString("name");
            int frequency = trackObj.getInt("playcount");
            duration = duration == 0 ? 200 : duration;
            String artistName = trackObj.getJSONObject("artist").getString("name");
            JSONArray image = trackObj.getJSONArray("image");
            JSONObject bigImage = image.getJSONObject(image.length() - 1);
            return new TrackDurationChart(bigImage.getString("#text"), size, name, artistName, null, frequency, frequency * duration, params.isWriteTitles(), params.isWritePlays(), params.isShowTime());
        };
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getDailyTrackParser(ChartGroupParameters params, Map<Track, Integer> durationsFromPeriod) {
        return (jsonObject, ignored) ->
        {
            NowPlayingArtist x = AlbumChart.fromRecentTrack(jsonObject, TopEntity.TRACK);
            Integer orDefault = durationsFromPeriod.getOrDefault(new Track(x.getArtistName(), x.getSongName(), 1, false, 0), 200);
            return new TrackDurationChart(null, 0, x.getSongName(), x.getArtistName(), x.getArtistMbid(),
                    1
                    , orDefault, params.isWriteTitles(), params.isWritePlays(), params.isShowTime());
        };
    }


    public boolean isShowDuration() {
        return showDuration;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getAlbumName(), ChartLine.Type.TITLE));
            list.add(new ChartLine(getArtistName()));
        }
        if (showDuration) {
            list.add(new ChartLine(String.format("%d:%02d hours", seconds / 3600, seconds / 60 % 60)));
        }
        if (drawPlays) {
            list.add(new ChartLine(getPlays() + " " + CommandUtil.singlePlural(getPlays(), "minutes", "plays")));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%s hours** in **%d** %s%n",
                CommandUtil.cleanMarkdownCharacter(getArtistName()),
                CommandUtil.cleanMarkdownCharacter(getAlbumName()),
                LinkUtils.getLastFMArtistTrack(getArtistName(), getAlbumName()),
                String.format("%d:%02d", seconds / 3600, seconds / 60 % 60),
                getPlays(),
                CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public String toChartString() {
        return String.format("%s - %s %sh",
                getArtistName(),
                getAlbumName(),
                String.format("%d:%02d", seconds / 3600, seconds / 60 % 60));
    }

    @Override
    public int getChartValue() {
        return getSeconds();
    }


}
