package core.imagerenderer.util;

import com.google.protobuf.MapEntry;
import core.parsers.params.CommandParameters;
import dao.entities.Language;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface IPieableMap<K, V, Y extends CommandParameters> extends IPieable<Map<K, V>, Y> {

    default Map<Boolean, Map<String, Integer>> getMappedData(Map<K, V> data, Function<K, String> keyMapping, ToIntFunction<V> valueMapping, Predicate<Map.Entry<K, V>> partitioner) {
        Map<Boolean, Map<String, Integer>> parted = new HashMap<>(2);
        parted.put(true, new HashMap<>());
        parted.put(false, new HashMap<>());
        var entries = parted.get(true);
        Set<String> values = new HashSet<>();
        AtomicInteger counter = new AtomicInteger(1);
        for (Map.Entry<K, V> x : data.entrySet()) {
            String newTitles = keyMapping.apply(x.getKey());
            if (values.contains(newTitles)) {
                newTitles += "\u200B".repeat(counter.getAndIncrement());
            } else {
                values.add(newTitles);
            }
            if (partitioner.test(x)) {
                entries.put(newTitles, valueMapping.applyAsInt(x.getValue()));
            } else {
                parted.get(false).put(newTitles, valueMapping.applyAsInt(x.getValue()));
            }
        }
        return parted;
    }


    @Override
    default void fillSeries(PieChart pieChart, Function<Map<K, V>, String> keyMapping, ToIntFunction<Map<K, V>> valueMapping, Predicate<Map<K, V>> partitioner, Map<K, V> data) {
        throw new UnsupportedOperationException();
    }

    @Override
    default  Map<Boolean, Map<String, Integer>> getData(Map<K, V> data, Function<Map<K, V>, String> keyMapping, ToIntFunction<Map<K, V>> valueMapping, Predicate<Map<K, V>> partitioner) {
        throw new UnsupportedOperationException();
    }

    default void fillMappedSeries(PieChart pieChart, Function<K, String> keyMapping, ToIntFunction<V> valueMapping, Predicate<Map.Entry<K, V>> partitioner, Map<K, V> data) {
        Map<Boolean, Map<String, Integer>> parted = getMappedData(data, keyMapping, valueMapping, partitioner);
        AtomicInteger counter = new AtomicInteger(1);
        Map<String, Integer> entries = parted.get(true);
        int sum = parted.get(false).values().stream().mapToInt(i -> i).sum();
        entries.entrySet().stream().sorted((x, y) -> y.getValue().compareTo(x.getValue()))
                .forEachOrdered(entry -> {
                    int i = counter.incrementAndGet();
                    String key = entry.getKey();
                    try {
                        pieChart.addSeries(key.isBlank() ? "\u200B" : key, entry.getValue());
                    } catch (IllegalArgumentException ex) {
                        pieChart.addSeries("\u200B".repeat(i) + key, entry.getValue());
                    }
                });
        if (sum != 0) {
            //To avoid having an artist called others and colliding bc no duplicates allowed
            pieChart.addSeries("Others\u200B", sum);
        }
    }
}
