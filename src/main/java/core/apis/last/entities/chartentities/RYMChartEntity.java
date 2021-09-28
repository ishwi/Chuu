package core.apis.last.entities.chartentities;

import core.commands.utils.CommandUtil;
import core.imagerenderer.ChartLine;
import dao.entities.ScoredAlbumRatings;
import dao.utils.LinkUtils;

import java.util.ArrayList;
import java.util.List;

public class RYMChartEntity extends UrlCapsule {
    private final boolean drawTitles;
    private final boolean drawScore;
    private final boolean useNumber;
    private final long numberOfRatings;

    public RYMChartEntity(String url, int pos, String artistName, String albumName, boolean drawTitles, boolean drawScore, boolean useNumber, long numberOfRatings) {
        super(url, pos, artistName, albumName, null);
        this.drawTitles = drawTitles;
        this.drawScore = drawScore;
        this.useNumber = useNumber;
        this.numberOfRatings = numberOfRatings;
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
                    starts += "☆";
                list.add(new ChartLine(starts));
            } else {
                list.add(new ChartLine(toRatingText(false)));
            }

        }
        return list;

    }

    private String toRatingText(boolean bold) {
        if (bold) {
            return "**%s** in **%d** %s".formatted(ScoredAlbumRatings.formatter.format(getPlays() / 2f), numberOfRatings, CommandUtil.singlePlural(Math.toIntExact(numberOfRatings), "rating", "ratings"));
        } else {
            return "%s in %d %s".formatted(ScoredAlbumRatings.formatter.format(getPlays() / 2f), numberOfRatings, CommandUtil.singlePlural(Math.toIntExact(numberOfRatings), "rating", "ratings"));

        }
    }

    @Override
    public String toEmbedDisplay() {
        return String.format(". **[%s - %s](%s)** - %s%n",
                CommandUtil.escapeMarkdown(getArtistName())
                , CommandUtil.escapeMarkdown(getAlbumName()),
                LinkUtils.getLastFmArtistAlbumUrl(getArtistName(), getAlbumName()),
                toRatingText(true));
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
