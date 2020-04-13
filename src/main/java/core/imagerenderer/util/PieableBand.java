package core.imagerenderer.util;

import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.entities.AlbumUserPlays;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieableBand extends OptionalPie implements IPieable<AlbumUserPlays, ArtistParameters> {


    public PieableBand(Parser parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, ArtistParameters params, List<AlbumUserPlays> data) {
        int total = data.stream().mapToInt(AlbumUserPlays::getPlays).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        fillSeries(chart,
                x -> x.getArtist() + " - " + x.getAlbum(),
                AlbumUserPlays::getPlays,
                x -> {
                    if (acceptedCount.get() < 10 || (counter.get() < breakpoint && acceptedCount.get() < 15)) {
                        counter.addAndGet(x.getPlays());
                        acceptedCount.incrementAndGet();
                        return true;
                    } else {
                        return false;
                    }
                }, data);
        return chart;
    }
}
