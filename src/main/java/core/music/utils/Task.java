/*
 * MIT License
 *
 * Copyright (c) 2020 Melms Media LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 From Octave bot https://github.com/Stardust-Discord/Octave/ Modified for integrating with JAVA and the current bot
 */
package core.music.utils;


import core.util.ChuuFixedPool;
import dao.exceptions.ChuuServiceException;

import java.util.concurrent.*;

public final class Task {
    private static final ScheduledExecutorService executor = ChuuFixedPool.ofScheduled(2, "Task-cleaner-");
    private final long delay;
    private final TimeUnit unit;
    private final Runnable runnable;
    private Future<?> task;

    public Task(long delay, TimeUnit unit, Runnable runnable) {
        this.delay = delay;
        this.unit = unit;
        this.runnable = runnable;
    }

    public boolean isRunning() {
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

    public void start() {
        stop(false);
        task = executor.schedule(runnable, delay, unit);
    }

    public void stop(boolean interrupt) {
        if (task != null) {
            task.cancel(interrupt);
        }
        task = null;
    }
}
