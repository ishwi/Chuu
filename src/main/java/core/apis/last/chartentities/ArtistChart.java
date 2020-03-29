package core.apis.last.chartentities;

import core.commands.CommandUtil;
import core.parsers.params.ChartParameters;
import dao.entities.UrlCapsule;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ArtistChart extends UrlCapsule {
    final boolean drawTitles;
    final boolean drawPlays;

    public ArtistChart(String url, int pos, String artistName, int plays, boolean drawTitles, boolean drawPlays) {
        super(url, pos, null, artistName, null, plays);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getArtistParser(ChartParameters chartParameters) {
        return (artistObj, size) -> {
            String artistName = artistObj.getString("name");
            int plays = artistObj.getInt("playcount");
            return new ArtistChart(null, size, artistName, plays, chartParameters.isWriteTitles(), chartParameters.isWritePlays());
        };
    }

    @Override
    public List<String> getLines() {
        List<String> list = new ArrayList<>();
        if (drawTitles) {
            list.add(getArtistName());
        }
        if (drawPlays) {
            list.add(getPlays() + CommandUtil.singlePlural(getPlays(), "play", "plays"));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s](%s)** - **%d** %s\n",
                CommandUtil.cleanMarkdownCharacter(getArtistName()),
                CommandUtil.getLastFmArtistUrl(getArtistName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }
}

