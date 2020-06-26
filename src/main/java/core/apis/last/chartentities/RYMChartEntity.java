package core.apis.last.chartentities;

import core.commands.CommandUtil;
import core.imagerenderer.ChartLine;
import dao.entities.ScoredAlbumRatings;
import dao.entities.UrlCapsule;

import java.util.ArrayList;
import java.util.List;

public class RYMChartEntity extends UrlCapsule {
    private final boolean drawTitles;
    private final boolean drawScore;
    private final boolean useNumber;
    private final double average;

    public RYMChartEntity(String url, int pos, String albumName, String artistName, int plays, boolean drawTitles, boolean drawScore, boolean useNumber, double average) {
        super(url, pos, albumName, artistName, null, plays);
        this.drawTitles = drawTitles;
        this.drawScore = drawScore;
        this.useNumber = useNumber;
        this.average = average;
    }

    public RYMChartEntity(String url, int pos, String artistName, String albumName, boolean drawTitles, boolean drawScore, boolean useNumber, double average) {
        super(url, pos, artistName, albumName, null);
        this.drawTitles = drawTitles;
        this.drawScore = drawScore;
        this.useNumber = useNumber;
        this.average = average;
    }

    @Override
    public List<ChartLine> getLines() {
        List<ChartLine> list = new ArrayList<>();
        if (drawTitles) {
            list.add(new ChartLine(getAlbumName(), ChartLine.Type.TITLE));
            list.add(new ChartLine(getArtistName()));
        }
        if (drawScore) {
            if (!useNumber) {
                float number = getPlays() / 2f;
                String starts = "★".repeat((int) number);
                if (number % 1 != 0)
                    starts += "✮";
                list.add(new ChartLine(starts));
            } else
                list.add(new ChartLine(ScoredAlbumRatings.formatter.format(average / 2f)));

        }
        return list;

    }


    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - **%d** %s%n",
                CommandUtil.cleanMarkdownCharacter(getArtistName())
                , CommandUtil.cleanMarkdownCharacter(getAlbumName()),
                CommandUtil.getLastFmArtistAlbumUrl(getArtistName(), getAlbumName()),
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
        return 0;
    }

}
