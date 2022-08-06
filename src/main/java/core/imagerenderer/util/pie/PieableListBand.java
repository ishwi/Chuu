package core.imagerenderer.util.pie;

import core.imagerenderer.util.bubble.StringFrequency;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.entities.AlbumUserPlays;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieableListBand extends OptionalPie implements IPieableList<AlbumUserPlays, ArtistParameters> {


    public PieableListBand(Parser<?> parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, ArtistParameters params, List<AlbumUserPlays> data) {
        int total = data.stream().mapToInt(AlbumUserPlays::getPlays).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        IPieableList.fillListedSeries(chart,
                x -> x.getArtist() + " - " + x.getAlbum(),
                AlbumUserPlays::getPlays,
                data);
        return chart;
    }


    @Override
    public List<StringFrequency> obtainFrequencies(List<AlbumUserPlays> data, ArtistParameters params) {
        return data.stream().map(t -> new StringFrequency(t.getArtist() + " - " + t.getAlbum(), t.getPlays())).toList();

    }
}
