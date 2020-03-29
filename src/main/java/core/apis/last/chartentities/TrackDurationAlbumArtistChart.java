package core.apis.last.chartentities;

import com.sun.istack.NotNull;
import core.commands.CommandUtil;
import core.imagerenderer.ChartLine;
import core.parsers.params.ChartGroupParameters;
import dao.entities.AlbumInfo;
import dao.entities.UrlCapsule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TrackDurationAlbumArtistChart extends TrackDurationArtistChart {
    public TrackDurationAlbumArtistChart(String url, int pos, String trackName, String artistName, String mbid, int plays, int seconds, boolean drawTitles, boolean drawPlays, boolean showDuration) {
        super(url, pos, trackName, artistName, mbid, plays, seconds, drawTitles, drawPlays, showDuration);
    }

    @NotNull
    public static List<UrlCapsule> getGrouped(List<UrlCapsule> urlCapsules) {
        Map<AlbumInfo, List<UrlCapsule>> collect1 = urlCapsules.stream().collect(Collectors.groupingBy(x -> new AlbumInfo(x.getAlbumName(), x.getArtistName())));
        return collect1.entrySet().stream().map(x -> {
            AlbumInfo key = x.getKey();
            List<UrlCapsule> value = x.getValue();
            Optional<UrlCapsule> reduce = value.stream().reduce((urlCapsule, urlCapsule2) -> {
                urlCapsule.setPlays(urlCapsule.getPlays() + urlCapsule2.getPlays());
                if (urlCapsule instanceof TrackDurationArtistChart) {
                    TrackDurationArtistChart capsule = (TrackDurationArtistChart) urlCapsule;
                    if (urlCapsule2 instanceof TrackDurationArtistChart) {
                        TrackDurationArtistChart capsule2 = (TrackDurationArtistChart) urlCapsule2;
                        capsule.setSeconds(capsule.getSeconds() + capsule2.getSeconds());
                        return capsule;
                    }
                }
                return urlCapsule;
            });
            if (reduce.isPresent()) {
                UrlCapsule urlCapsule = reduce.get();
                urlCapsule.setArtistName(key.getArtist());
                urlCapsule.setAlbumName(key.getName());
                return urlCapsule;
            } else {
                return null;
            }
        }).collect(Collectors.toList());
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getParser(ChartGroupParameters parameters) {
        return (trackObj, size) -> {
            int duration = trackObj.getInt("duration");
            String name = trackObj.getString("name");
            int frequency = trackObj.getInt("playcount");
            duration = duration == 0 ? 200 : duration;
            String artist_name = trackObj.getJSONObject("artist").getString("name");
            String mbid = trackObj.getString("mbid");
            String url = null;
            if (trackObj.has("album")) {
                JSONObject album = trackObj.getJSONObject("album");
                name = album.getString("name");
                if (album.has("image")) {
                    JSONArray ar = album.getJSONArray("image");
                    url = ar.getJSONObject(ar.length() - 1).getString("#text");
                }
            }
            return new TrackDurationAlbumArtistChart(url, size, name, artist_name, mbid, frequency, frequency * duration, parameters.isWriteTitles(), parameters.isWritePlays(), parameters.isShowTime());
        };
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

    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%s hours** in **%d** %s\n",
                CommandUtil.cleanMarkdownCharacter(getArtistName()),
                CommandUtil.cleanMarkdownCharacter(getAlbumName()),
                CommandUtil.getLastFmArtistAlbumUrl(getArtistName(), getAlbumName()),
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
}
