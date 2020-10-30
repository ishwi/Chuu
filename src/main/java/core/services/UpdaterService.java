package core.services;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UpdaterService {
    private static final Set<String> locks = ConcurrentHashMap.newKeySet();


    public static boolean lockAndContinue(String lastfmId) {
        synchronized (locks) {
            if (locks.contains(lastfmId)) {
                return false;
            }
            locks.add(lastfmId);
            return true;
        }
    }


    public static boolean remove(String lastfmId) {
        synchronized (locks) {
            return (locks.remove(lastfmId));
        }
    }
}
