package core.imagerenderer.util;

import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.entities.ReturnNowPlaying;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieableKnows extends OptionalPie implements IPieable<ReturnNowPlaying, ArtistParameters> {

    public PieableKnows(Parser parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, ArtistParameters params, List<ReturnNowPlaying> data) {
        int total = data.stream().mapToInt(ReturnNowPlaying::getPlayNumber).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        fillSeries(chart,
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

}
