package core.imagerenderer.util;

import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.CommandParameters;
import dao.entities.AlbumUserPlays;
import dao.entities.Language;
import org.knowm.xchart.PieChart;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class IPieableLanguage extends OptionalPie implements IPieableMap<Language, Long, CommandParameters> {
    public IPieableLanguage(Parser<?> parser) {
        super(parser);
    }


    @Override
    public PieChart fillPie(PieChart chart, CommandParameters params, Map<Language, Long> data) {
        long total = data.values().stream().mapToLong(Long::longValue).sum();
        int breakpoint = (int) (0.75 * total);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0);
        fillMappedSeries(chart,
                Language::getName,
                Long::intValue,
                x -> {
                    if (acceptedCount.get() < 10 || (counter.get() < breakpoint && acceptedCount.get() < 15)) {
                        counter.addAndGet(x.getValue().intValue());
                        acceptedCount.incrementAndGet();
                        return true;
                    } else {
                        return false;
                    }
                }, data);
        return chart;
    }


}
