package core.music.utils;


import dao.exceptions.ChuuServiceException;

import java.util.concurrent.*;

public final class Task {
    private Future<?> task;
    private final long delay;
    private final TimeUnit unit;
    private final Runnable runnable;
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public final boolean isRunning() {
        if (task == null) {
            return false;
        }
        try {
            // Is that a good idea? Counting on exceptions looks weird.
            task.get(0, TimeUnit.MICROSECONDS);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChuuServiceException(e);
        } catch (ExecutionException | CancellationException e) {
            return false;
        } catch (TimeoutException e) {
            return true;
        }
    }

    public final void start() {
        stop(false);
        task = executor.schedule(runnable, delay, unit);
    }

    public final void stop(boolean interrupt) {
        if (task != null) {
            task.cancel(interrupt);
        }
        task = null;
    }

    public Task(long delay, TimeUnit unit, Runnable runnable) {
        this.delay = delay;
        this.unit = unit;
        this.runnable = runnable;
    }
}
