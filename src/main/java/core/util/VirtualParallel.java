package core.util;

import core.Chuu;
import dao.exceptions.ChuuServiceException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;


public class VirtualParallel {
    private VirtualParallel() {
    }

    public static <T, J> List<T> runIO(List<J> items, CheckedFunction<J, T> IOMapper) {
        return runIO(items, IOMapper, Instant.now().plus(1, ChronoUnit.MINUTES));
    }

    public static <T, J> List<T> runIO(List<J> items, CheckedFunction<J, T> IOMapper, Instant timeout) {
        return runIO(items, -1L, IOMapper, timeout);
    }

    public static <T, J> List<T> runIO(List<J> items, long limit, CheckedFunction<J, T> IOMapper) {
        return runIO(items, limit, IOMapper, Instant.now().plus(1, ChronoUnit.MINUTES));
    }

    public static <T, J> List<T> runIO(List<J> items, long limit, CheckedFunction<J, T> IOMapper, Instant timeout) {
        Supplier<CustomPools<T>> scoper;


        if (limit <= 0) {
            scoper = ExecuteAllIgnoreErrors::new;
        } else {
            scoper = () -> new ExecuteSome<>(limit);
        }
        try (var scope = scoper.get()) {

            outer:
            for (int i = 0, toProcessSize = items.size(); i < toProcessSize; i++) {
                J z = items.get(i);
                scope.fork(() -> IOMapper.apply(z));
                switch (scope) {
                    case ExecuteSome<T> _ -> {
                        if (i > limit) {
                            break outer;
                        }
                    }
                    case ExecuteAllIgnoreErrors<T> _ -> {
                    }
                }
            }
            if (timeout != null) {
                scope.joinUntil(timeout);
            } else {
                scope.join();
            }
            return scope.results();
        } catch (InterruptedException | TimeoutException e) {
            throw new ChuuServiceException(e);
        }
    }

    public static void handleInterrupt() {
        if (Thread.currentThread().isInterrupted()) {
            Chuu.getLogger().warn("Thread interrupted", new Exception());
            throw new ChuuServiceException(new InterruptedException());
        }
    }

    @FunctionalInterface
    public interface CheckedFunction<J, T> {
        T apply(J item) throws Exception;
    }

    private static sealed abstract class CustomPools<T> extends StructuredTaskScope<T> {
        public CustomPools() {
            super("Custom-pool", Thread.ofVirtual().name("Custom-pool").factory());
        }

        abstract List<T> results();
    }

    private static final class ExecuteSome<T> extends CustomPools<T> {

        private final AtomicInteger successCounter = new AtomicInteger(0);
        private final List<T> results = new ArrayList<>();
        // sanity check:
        private final AtomicInteger failCounter = new AtomicInteger(0);
        private final long numTasksForSuccess;

        public ExecuteSome(long numTasksForSuccess) {
            this.numTasksForSuccess = numTasksForSuccess;
        }


        @Override
        protected void handleComplete(Subtask<? extends T> subtask) {
            switch (subtask.state()) {
                case SUCCESS -> {
                    int numSuccess = successCounter.incrementAndGet();
                    if (numSuccess <= numTasksForSuccess) {
                        results.add(subtask.get());
                    }
                    if (numSuccess == numTasksForSuccess) {
                        shutdown();
                    }
                }
                case FAILED -> failCounter.incrementAndGet();
                case UNAVAILABLE -> throw new ChuuServiceException(new InterruptedException());
            }
        }

        //
        public List<T> results() {
            return results.stream().filter(Objects::nonNull).toList();
        }
    }

    public static final class ExecuteAllIgnoreErrors<T> extends CustomPools<T> {
        private final AtomicInteger forkCount = new AtomicInteger(0);
        private final AtomicInteger preJoinCount = new AtomicInteger(0);

        private final AtomicBoolean isInCollection = new AtomicBoolean(false);
        private final Lock readLock;
        private final Lock writeLock;
        private final Collection<T> results = new ConcurrentLinkedDeque<>();
        private final AtomicInteger failCounter = new AtomicInteger(0);

        {
            var lock = new ReentrantReadWriteLock();
            readLock = lock.readLock();
            writeLock = lock.writeLock();
        }

        public ExecuteAllIgnoreErrors() {
            super();
        }

        @Override
        public <U extends T> Subtask<U> fork(Callable<? extends U> task) {
            forkCount.incrementAndGet();
            forkCount.addAndGet(preJoinCount.get());
            return super.fork(task);
        }

        @Override
        public StructuredTaskScope<T> joinUntil(Instant deadline) throws InterruptedException, TimeoutException {
            isInCollection.compareAndSet(false, true);
            return super.joinUntil(deadline);
        }

        @Override
        public StructuredTaskScope<T> join() throws InterruptedException {
            isInCollection.compareAndSet(false, true);
            return super.join();
        }

        @Override
        protected void handleComplete(Subtask<? extends T> future) {
            var state = future.state();
            switch (state) {
                case SUCCESS -> {
                    writeLock.lock();
                    try {
                        T e = future.get();
                        if (e != null) {
                            results.add(e);
                        }
                    } finally {
                        writeLock.unlock();
                    }
                }
                case FAILED -> failCounter.incrementAndGet();
                case UNAVAILABLE -> throw new ChuuServiceException(new InterruptedException());
            }
            if (isInCollection.get()) {
                int i = forkCount.decrementAndGet();
                if (i == 0) {
                    shutdown();
                }
            } else {
                preJoinCount.incrementAndGet();
            }
        }

        //
        public List<T> results() {
            readLock.lock();
            try {
                return results.stream().filter(Objects::nonNull).toList();
            } finally {
                readLock.unlock();
            }
        }

    }
}
