package core.apis.last.chartentities;

import core.commands.CommandUtil;
import core.parsers.params.ChartParameters;
import dao.entities.UrlCapsule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class AlbumChart extends UrlCapsule {
    final boolean drawTitles;
    final boolean drawPlays;

    public AlbumChart(String url, int pos, String albumName, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays) {
        super(url, pos, albumName, artistName, mbid, plays);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
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
            return new AlbumChart(bigImage.getString("#text"), size, albumName, artistName, mbid, plays, chartParameters.isWriteTitles(), chartParameters.isWritePlays());
        };

    }

    @Override
    public List<String> getLines() {
        List<String> list = new ArrayList<>();
        if (drawTitles) {
            list.add(getArtistName());
            list.add(getAlbumName());
        }
        if (drawPlays) {
            list.add(getPlays() + CommandUtil.singlePlural(getPlays(), "play", "plays"));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%d** %s\n",
                CommandUtil.cleanMarkdownCharacter(getArtistName())
                , CommandUtil.cleanMarkdownCharacter(getAlbumName()),
                CommandUtil.getLastFmArtistAlbumUrl(getArtistName(), getAlbumName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

}
