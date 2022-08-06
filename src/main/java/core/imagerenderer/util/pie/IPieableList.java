package core.imagerenderer.util.pie;

import core.imagerenderer.util.bubble.IBubbleable;
import core.parsers.params.CommandParameters;
import org.knowm.xchart.PieChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public interface IPieableList<T, Y extends CommandParameters> extends IPieable<List<T>, Y>, IBubbleable<List<T>, Y> {


    static <T> Map<Boolean, Map<String, Long>> getListedData(List<T> data, Function<T, String> keyMapping, ToLongFunction<T> valueMapping) {
        Map<Boolean, Map<String, Long>> parted = new HashMap<>(2);
        parted.put(true, new HashMap<>());
        parted.put(false, new HashMap<>());
        var entries = parted.get(true);
        var others = parted.get(false);
        Set<String> values = new HashSet<>();
        AtomicInteger counter = new AtomicInteger(1);
        data.stream().limit(12).forEach(x -> {
            String newTitles = keyMapping.apply(x);
            if (values.contains(newTitles)) {
                newTitles += "​".repeat(counter.getAndIncrement());
            } else {
                values.add(newTitles);
            }
            entries.put(newTitles, valueMapping.applyAsLong(x));
        });
        long sum = data.stream().skip(12).mapToLong(valueMapping).sum();
        others.put("Others​", sum);
        return parted;
    }

    static <T> void fillListedSeries(PieChart pieChart, Function<T, String> keyMapping, ToLongFunction<T> valueMapping, List<T> data) {
        Map<Boolean, Map<String, Long>> parted = getListedData(data, keyMapping, valueMapping);
        AtomicInteger counter = new AtomicInteger(1);
        Map<String, Long> entries = parted.get(true);
        long sum = parted.get(false).values().stream().mapToLong(i -> i).sum();
        entries.entrySet().stream().sorted((x, y) -> y.getValue().compareTo(x.getValue()))
                .forEachOrdered(entry -> {
                    int i = counter.incrementAndGet();
                    String key = entry.getKey();
                    try {
                        pieChart.addSeries(key.isBlank() ? "​" : key, entry.getValue());
                    } catch (IllegalArgumentException ex) {
                        pieChart.addSeries("​".repeat(i) + key, entry.getValue());
                    }
                });
        if (sum != 0) {
            //To avoid having an artist called others and colliding bc no duplicates allowed
            pieChart.addSeries("Others​", sum);
        }
    }

    @Override
    default Map<Boolean, Map<String, Integer>> getData(List<T> data, Function<List<T>, String> keyMapping, ToIntFunction<List<T>> valueMapping, Predicate<List<T>> partitioner) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void fillSeries(PieChart pieChart, Function<List<T>, String> keyMapping, ToIntFunction<List<T>> valueMapping, Predicate<List<T>> partitioner, List<T> data) {
        throw new UnsupportedOperationException();
    }

}

