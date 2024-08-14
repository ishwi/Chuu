package core.commands.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ListUtils {

    public static <K, T> BiFunction<K, List<T>, List<T>> computeRemoval(T value) {
        return (K aLong, List<T> myCommands) -> {
            if (myCommands != null) {
                myCommands.remove(value);
                if (myCommands.isEmpty()) {
                    return null;
                }
            }
            return myCommands;
        };
    }

    public static <K, T> boolean hasMapping(Map<K, List<T>> map, K key, T value) {
        return map.getOrDefault(key, Collections.emptyList()).contains(value);
    }
}
