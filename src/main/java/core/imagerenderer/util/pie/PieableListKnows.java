package core.imagerenderer.util.pie;

import core.imagerenderer.util.bubble.StringFrequency;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.entities.ReturnNowPlaying;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieableListKnows<T extends CommandParameters> extends OptionalPie implements IPieableList<ReturnNowPlaying, T> {

    public PieableListKnows(Parser<?> parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, T params, List<ReturnNowPlaying> data) {
        long total = data.stream().mapToLong(ReturnNowPlaying::getPlayNumber).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        IPieableList.fillListedSeries(chart,
                ReturnNowPlaying::getDiscordName,
                ReturnNowPlaying::getPlayNumber,
                data);
        return chart;
    }


    @Override
    public List<StringFrequency> obtainFrequencies(List<ReturnNowPlaying> data, T params) {
        return data.stream().map(t -> new StringFrequency(t.getDiscordName(), t.getPlayNumber())).toList();

    }
}
