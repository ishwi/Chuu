package core.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;

public class FastUtilsInit {


    public static <T> Long2ObjectMap<T> init(Map<Long, T> map) {
        Long2ObjectOpenHashMap<T> init = new Long2ObjectOpenHashMap<>(map.size());
        init.putAll(map);
        return init;
    }
}
