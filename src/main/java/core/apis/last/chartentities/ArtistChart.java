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
import java.util.Objects;
import java.util.function.BiFunction;

public class ArtistChart extends UrlCapsule {
    final boolean drawTitles;
    final boolean drawPlays;

    public ArtistChart(String url, int pos, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays) {
        super(url, pos, null, artistName, mbid, plays);
        this.drawTitles = drawTitles;
        this.drawPlays = drawPlays;
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getArtistParser(ChartParameters chartParameters) {
        return (artistObj, size) -> {
            String artistName = artistObj.getString("name");
            int plays = artistObj.getInt("playcount");
            String mbid = artistObj.getString("mbid");
            return new ArtistChart(null, size, artistName, mbid, plays, chartParameters.isWriteTitles(), chartParameters.isWritePlays());
        };
    }

    public static BiFunction<JSONObject, Integer, UrlCapsule> getDailyArtistParser(ChartParameters chartParameters) {
        return (jsonObject, ignored) ->
        {
            NowPlayingArtist x = AlbumChart.fromRecentTrack(jsonObject, TopEntity.ARTIST);
            return new ArtistChart(x.getUrl(), 0, x.getArtistName(), x.getMbid(), 1,
                    chartParameters.isWriteTitles()
                    , chartParameters.isWritePlays());
        };
    }


    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getArtistName(), ChartLine.Type.TITLE));
        }
        if (drawPlays) {
            list.add(new ChartLine(getPlays() + CommandUtil.singlePlural(getPlays(), " play", " plays")));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s](%s)** - **%d** %s%n",
                CommandUtil.cleanMarkdownCharacter(getArtistName()),
                CommandUtil.getLastFmArtistUrl(getArtistName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public String toChartString() {
        return String.format("%s%n%d %s", getArtistName(), getPlays(), CommandUtil.singlePlural(getPlays(), "play", "plays"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtistChart)) return false;
        if (!super.equals(o)) return false;
        ArtistChart that = (ArtistChart) o;
        return this.getArtistName().equals(that.getArtistName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getArtistName());
    }

    @Override
    public int getChartValue() {
        return getPlays();
    }
}

