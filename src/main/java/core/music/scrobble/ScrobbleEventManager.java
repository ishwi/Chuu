package core.music.scrobble;

import core.Chuu;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScrobbleEventManager {
    private static final BlockingQueue<ScrobbleStatus> queue = new ArrayBlockingQueue<>(100);
    private static final AtomicBoolean doLoop = new AtomicBoolean(true);
    private final StatusProcessor processor;

    public ScrobbleEventManager(StatusProcessor statusProcessor) {
        processor = statusProcessor;
        Thread.ofVirtual().name("Scrobble Manager")
                .allowSetThreadLocals(false)
                .inheritInheritableThreadLocals(false)
                .uncaughtExceptionHandler((t, e) -> Chuu.getLogger().warn(e.getMessage(), e))
                .start(new ScrobbleLoop());
    }

    public void submitEvent(ScrobbleStatus status) {
        queue.add(status);
    }

    private final class ScrobbleLoop implements Runnable {
        private final ThreadFactory tf = Thread.ofVirtual().name("ScrobbleProcessor", 0)
                .allowSetThreadLocals(false)
                .inheritInheritableThreadLocals(false)
                .uncaughtExceptionHandler((t, e) -> Chuu.getLogger().warn(e.getMessage(), e)).factory();

        @Override
        public void run() {

            while (doLoop.get()) {
                try {
                    ScrobbleStatus take = queue.take();
                    tf.newThread(() -> {
                                try {
                                    processor.process(take);
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
