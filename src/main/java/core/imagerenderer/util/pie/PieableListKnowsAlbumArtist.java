package core.imagerenderer.util.pie;

import core.imagerenderer.util.bubble.StringFrequency;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.entities.ReturnNowPlaying;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieableListKnowsAlbumArtist extends OptionalPie implements IPieableList<ReturnNowPlaying, ArtistAlbumParameters> {

    public PieableListKnowsAlbumArtist(Parser<?> parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, ArtistAlbumParameters ignored, List<ReturnNowPlaying> data) {
        int total = data.stream().mapToInt(ReturnNowPlaying::getPlayNumber).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        fillListedSeries(chart,
                ReturnNowPlaying::getDiscordName,
                ReturnNowPlaying::getPlayNumber,
                x -> {
                    if (acceptedCount.get() < 10 || (counter.get() < breakpoint && acceptedCount.get() < 15)) {
                        counter.addAndGet(x.getPlayNumber());
                        acceptedCount.incrementAndGet();
                        return true;
                    } else {
                        return false;
                    }
                }, data);
        return chart;
    }

    @Override
    public List<StringFrequency> obtainFrequencies(List<ReturnNowPlaying> data, ArtistAlbumParameters params) {
        return data.stream().map(t -> new StringFrequency(t.getDiscordName(), t.getPlayNumber())).toList();
    }
}
