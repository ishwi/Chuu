package core.apis.last.chartentities;

import core.apis.last.TopEntity;
import core.commands.CommandUtil;
import core.imagerenderer.ChartLine;
import core.parsers.params.ChartGroupParameters;
import dao.entities.NowPlayingArtist;
import dao.entities.Track;
import dao.entities.UrlCapsule;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class TrackDurationArtistChart extends TrackDurationChart {


    public TrackDurationArtistChart(String url, int pos, String trackName, String artistName, String mbid, int plays, int seconds, boolean drawTitles, boolean drawPlays, boolean showDuration) {
        super(url, pos, trackName, artistName, mbid, plays, seconds, drawTitles, drawPlays, showDuration);

    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getTrackDurationArtistParser(ChartGroupParameters params) {

        return (trackObj, size) -> {
            int duration = trackObj.getInt("duration");
            String name = trackObj.getString("name");
            int frequency = trackObj.getInt("playcount");
            duration = duration == 0 ? 200 : duration;
            String artistName = trackObj.getJSONObject("artist").getString("name");
            return new TrackDurationArtistChart(null, size, name, artistName, null, frequency, frequency * duration, params.isWriteTitles(), params.isWritePlays(), params.isShowTime());
        };
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getDailyTrackDurationArtistParser(ChartGroupParameters params, Map<Track, Integer> durationsFromPeriod) {
        return (jsonObject, ignored) ->
        {
            NowPlayingArtist x = AlbumChart.fromRecentTrack(jsonObject, TopEntity.ARTIST);
            Integer orDefault = durationsFromPeriod.getOrDefault(new Track(x.getArtistName(), x.getSongName(), 1, false, 0), 200);
            return new TrackDurationArtistChart(x.getUrl(), 0, x.getSongName(), x.getArtistName(), x.getMbid(),
                    1
                    , orDefault, params.isWriteTitles(), params.isWritePlays(), params.isShowTime());
        };
    }


    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getArtistName(), ChartLine.Type.TITLE));
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
        return String.format(". **[%s](%s)** - **%s** hours in **%d** %s%n",
                CommandUtil.cleanMarkdownCharacter(getArtistName()),
                CommandUtil.getLastFmArtistUrl(getArtistName()),
                String.format("%d:%02d", seconds / 3600, seconds / 60 % 60),
                getPlays(),
                CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public String toChartString() {
        return String.format("%s %sh",
                getArtistName(),
                String.format("%d:%02d", seconds / 3600, seconds / 60 % 60));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackDurationArtistChart)) return false;
        if (!super.equals(o)) return false;
        TrackDurationArtistChart that = (TrackDurationArtistChart) o;
        return this.getArtistName().equals(that.getArtistName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getArtistName());
    }
}
