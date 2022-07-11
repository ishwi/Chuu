package core.music.scrobble;

import core.Chuu;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScrobbleEventManager {
    private static final BlockingQueue<ScrobbleStatus> queue = new ArrayBlockingQueue<>(100);
    private static final AtomicBoolean doLoop = new AtomicBoolean(true);
    private final StatusProcesser processer;

    public ScrobbleEventManager(StatusProcesser statusProcesser) {
        processer = statusProcesser;
        Thread.ofVirtual().name("Scrobble Manager").start(new ScrobbleLoop());
    }

    public void submitEvent(ScrobbleStatus status) {
        queue.add(status);
    }

    private final class ScrobbleLoop implements Runnable {
        private final ThreadFactory tf = Thread.ofVirtual().name("ScrobbleProcessor", 0).factory();

        @Override
        public void run() {

            while (doLoop.get()) {
                try {
                    ScrobbleStatus take = queue.take();
                    tf.newThread(() -> {
                                try {
                                    processer.process(take);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .start();
                } catch (Exception e) {
                    Chuu.getLogger().info("Thread interrupted");
                    Chuu.getLogger().info(e.getMessage(), e);
                }
            }
        }
    }


}
