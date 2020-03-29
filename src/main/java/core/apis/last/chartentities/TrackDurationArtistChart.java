package core.apis.last.chartentities;

import core.commands.CommandUtil;
import core.parsers.params.ChartGroupParameters;
import dao.entities.UrlCapsule;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class TrackDurationArtistChart extends TrackDurationChart {


    public TrackDurationArtistChart(String url, int pos, String trackName, String artistName, String mbid, int plays, int seconds, boolean drawTitles, boolean drawPlays, boolean showDuration) {
        super(url, pos, trackName, artistName, mbid, plays, seconds, drawTitles, drawPlays, showDuration);

    }

    public TrackDurationArtistChart(String url, int pos, String trackName, String albumName, String mbid, boolean drawTitles, boolean drawPlays, boolean showDuration, int seconds) {
        super(url, pos, trackName, albumName, mbid, drawTitles, drawPlays, showDuration, seconds);
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getTrackDurationArtistParser(ChartGroupParameters params) {

        return (trackObj, size) -> {
            int duration = trackObj.getInt("duration");
            String name = trackObj.getString("name");
            int frequency = trackObj.getInt("playcount");
            duration = duration == 0 ? 200 : duration;
            String artist_name = trackObj.getJSONObject("artist").getString("name");
            return new TrackDurationArtistChart(null, size, name, artist_name, null, frequency, frequency * duration, params.isWriteTitles(), params.isWritePlays(), params.isShowTime());
        };
    }

    @Override
    public List<String> getLines() {
        List<String> list = new ArrayList<>();
        if (drawTitles) {
            list.add(getArtistName());
        }
        if (showDuration) {
            list.add(String.format("%d:%02d hours", seconds / 3600, seconds / 60 % 60));

        }
        if (drawPlays) {
            list.add(getPlays() + " " + CommandUtil.singlePlural(getPlays(), "minutes", "plays"));
        }
        return list;
    }

    public String toEmbedDisplay() {
        return String.format(". **[%s](%s)** - **%s** hours in **%d** %s\n",
                CommandUtil.cleanMarkdownCharacter(getArtistName()),
                CommandUtil.getLastFmArtistUrl(getArtistName()),
                String.format("%d:%02d", seconds / 3600, seconds / 60 % 60),
                getPlays(),
                CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }
}
