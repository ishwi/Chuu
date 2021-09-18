package core.music.scrobble;

import core.Chuu;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ScrobbleEventManager {
    private static final BlockingQueue<ScrobbleStatus> queue = new ArrayBlockingQueue<>(100);
    private static final int CONSUMER_COUNT = 3;
    private static final AtomicBoolean doLoop = new AtomicBoolean(true);
    private final ThreadPoolExecutor manager;
    private final StatusProcesser processer;

    {
        AtomicInteger ranker = new AtomicInteger();
        manager = new ThreadPoolExecutor(CONSUMER_COUNT, CONSUMER_COUNT, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(CONSUMER_COUNT),
                r -> new Thread(r, "Scrobble-executor-" + ranker.getAndIncrement()),
                (r, executor) -> Chuu.getLogger().warn("TRYING TO CREATE MORE THAN 3 THREADS ON SCROBBLE LOOP")
        );
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            manager.submit(new ScrobbleLoop());
        }
    }

    public ScrobbleEventManager(StatusProcesser statusProcesser) {
        processer = statusProcesser;
    }

    public void submitEvent(ScrobbleStatus status) {
        queue.add(status);
    }

    private final class ScrobbleLoop implements Runnable {

        @Override
        public void run() {
            while (doLoop.get()) {
                try {
                    processer.process(queue.take());
                } catch (Exception e) {
                    Chuu.getLogger().info("Thread interrupted");
                    Chuu.getLogger().info(e.getMessage(), e);
                }
            }
        }
    }


}
