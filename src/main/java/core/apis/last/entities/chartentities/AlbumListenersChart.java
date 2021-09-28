package core.apis.last.entities.chartentities;

import core.commands.utils.CommandUtil;
import core.imagerenderer.ChartLine;
import dao.utils.LinkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumListenersChart extends AlbumChart {


    public AlbumListenersChart(String url, int pos, String albumName, String artistName, String mbid, int plays, boolean drawTitles, boolean drawPlays, boolean isAside) {
        super(url, pos, albumName, artistName, mbid, plays, drawTitles, drawPlays, isAside);
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
            list.add(new ChartLine(getPlays() + CommandUtil.singlePlural(getPlays(), " listener", " listeners")));
        }
        return list;
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%d** %s%n",
                CommandUtil.escapeMarkdown(getArtistName())
                , CommandUtil.escapeMarkdown(getAlbumName()),
                LinkUtils.getLastFmArtistAlbumUrl(getArtistName(), getAlbumName()),
                getPlays(), CommandUtil.singlePlural(getPlays(), "listener", "listeners"));
    }

    @Override
    public String toChartString() {
        return String.format("%s - %s%n%d %s",
                getArtistName(),
                getAlbumName(), getPlays(), CommandUtil.singlePlural(getPlays(), "listener", "listeners"));
    }

    @Override
    public int getChartValue() {
        return getPlays();
    }

}
