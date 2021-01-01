package core.apis.last.chartentities;

import core.apis.last.TopEntity;
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

public class AlbumChart extends UrlCapsule {
    final boolean drawTitles;
    final boolean drawPlays;
    final boolean isAside;


    public AlbumChart(String url, int pos, String albumName, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays, boolean isAside) {
        super(url, pos, albumName, artistName, mbid, plays);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
        this.isAside = isAside;
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getAlbumParser(ChartParameters chartParameters) {
        return (albumObj, size) ->
        {
            JSONObject artistObj = albumObj.getJSONObject("artist");
            String albumName = albumObj.getString("name");
            String artistName = artistObj.getString("name");
            JSONArray image = albumObj.getJSONArray("image");
            String mbid = albumObj.getString("mbid");
            int plays = albumObj.getInt("playcount");
            JSONObject bigImage = image.getJSONObject(image.length() - 1);
            return new AlbumChart(bigImage.getString("#text"), size, albumName, artistName, mbid, plays, chartParameters.isWriteTitles(), chartParameters.isWritePlays(), chartParameters.isAside());
        };

    }

    public static NowPlayingArtist fromRecentTrack(JSONObject trackObj, TopEntity topEntity) {
        JSONObject artistObj = trackObj.getJSONObject("artist");
        String artistName = artistObj.getString("#text");
        JSONObject album = trackObj.getJSONObject("album");
        String mbid;
        switch (topEntity) {
            case ALBUM:
                mbid = album.getString("mbid");
                break;
            case TRACK:
                mbid = trackObj.getString("mbid");
                break;
            case ARTIST:
                mbid = artistObj.getString("mbid");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + topEntity);
        }
        JSONArray image = trackObj.getJSONArray("image");
        JSONObject bigImage = image.getJSONObject(image.length() - 1);
        String albumName = album.getString("#text");
        String songName = trackObj.getString("name");
        return new NowPlayingArtist(artistName, mbid, false, albumName, songName, bigImage.getString("#text"), null);
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getDailyAlbumParser(ChartParameters chartParameters) {
        return (jsonObject, ignored) ->
        {
            NowPlayingArtist x = fromRecentTrack(jsonObject, TopEntity.ALBUM);
            return new AlbumChart(x.getUrl(), 0, x.getAlbumName(), x.getArtistName(), x.getArtistMbid(), 1,
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
            list.add(new ChartLine(getPlays() + CommandUtil.singlePlural(getPlays(), " play", " plays")));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%d** %s%n",
                CommandUtil.cleanMarkdownCharacter(getArtistName())
                , CommandUtil.cleanMarkdownCharacter(getAlbumName()),
                LinkUtils.getLastFmArtistAlbumUrl(getArtistName(), getAlbumName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public String toChartString() {
        return String.format("%s - %s%n%d %s",
                getArtistName(),
                getAlbumName(), getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public int getChartValue() {
        return getPlays();
    }

}
