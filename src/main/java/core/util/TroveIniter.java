package core.util;

import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Map;

public class TroveIniter {


    public static <T> TLongObjectHashMap<T> init(Map<Long, T> map) {
        TLongObjectHashMap<T> init = new TLongObjectHashMap<>(map.size());
        init.putAll(map);
        return init;
    }
}
