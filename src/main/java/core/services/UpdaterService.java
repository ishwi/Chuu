package core.services;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UpdaterService {
    private static final ReentrantLock reentrantLock = new ReentrantLock();
    private static final Set<String> locks = ConcurrentHashMap.newKeySet();


    public static boolean lockAndContinue(String lastfmId) {
        reentrantLock.lock();
        try {
            if (locks.contains(lastfmId)) {
                return false;
            }
            locks.add(lastfmId);
            return true;
        } finally {
            reentrantLock.unlock();
        }
    }


    public static boolean remove(String lastfmId) {
        reentrantLock.lock();
        try {
            return (locks.remove(lastfmId));
        } finally {
            reentrantLock.unlock();
        }
    }
}
