package core.imagerenderer.util.pie;

import core.commands.utils.CommandUtil;
import core.imagerenderer.util.bubble.StringFrequency;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.entities.Track;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieableListTrack extends OptionalPie implements IPieableList<Track, ArtistAlbumParameters> {

    public PieableListTrack(Parser<?> parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, ArtistAlbumParameters params, List<Track> data) {
        int total = data.stream().mapToInt(Track::getPlays).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        IPieableList.fillListedSeries(chart,
                (x) -> x.getName() + " - " + x.getPlays() + CommandUtil.singlePlural(x.getPlays(), " play", " plays"),
                Track::getPlays,
                data);
        return chart;
    }

    @Override
    public List<StringFrequency> obtainFrequencies(List<Track> data, ArtistAlbumParameters params) {
        return data.stream().map((x) -> new StringFrequency(x.getName() + " - " + x.getPlays() + CommandUtil.singlePlural(x.getPlays(), " play", " plays"), x.getPlays())).toList();
    }
}
