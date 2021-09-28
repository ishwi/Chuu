package core.apis.last.entities.chartentities;

import core.commands.utils.CommandUtil;
import core.imagerenderer.ChartLine;
import dao.utils.LinkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArtistListenersChart extends ArtistChart {


    public ArtistListenersChart(String url, int pos, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays, boolean isAside) {
        super(url, pos, artistName, mbid, plays, drawTitles, drawPlays, isAside);
    }

    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getArtistName(), ChartLine.Type.TITLE));
        }
        if (drawPlays) {
            list.add(new ChartLine(getPlays() + CommandUtil.singlePlural(getPlays(), " listener", " listeners")));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s](%s)** - **%d** %s%n",
                CommandUtil.escapeMarkdown(getArtistName()),
                LinkUtils.getLastFmArtistUrl(getArtistName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "listener", "listeners"));
    }

    @Override
    public String toChartString() {
        return String.format("%s%n%d %s", getArtistName(), getPlays(), CommandUtil.singlePlural(getPlays(), "listener", "listeners"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtistListenersChart that)) return false;
        if (!super.equals(o)) return false;
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

