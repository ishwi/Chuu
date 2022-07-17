package core.util;

import core.Chuu;
import dao.exceptions.ChuuServiceException;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


public class VirtualParallel {
    private VirtualParallel() {
    }

    public static <T, J> List<T> runIO(List<J> items, CheckedFunction<J, T> IOMapper) {
        return runIO(items, -1L, IOMapper);
    }

    public static <T, J> List<T> runIO(List<J> items, long limit, CheckedFunction<J, T> IOMapper) {
        Supplier<CustomPools<T>> scoper;
        if (limit <= 0) {
            scoper = ExecuteAllIgnoreErrors::new;
        } else {
            scoper = () -> new ExecuteSome<>(limit);
            ;
        }
        try (var scope = scoper.get()) {

            outer:
            for (int i = 0, toProcessSize = items.size(); i < toProcessSize; i++) {
                J z = items.get(i);
                scope.fork(() -> IOMapper.apply(z));
                switch (scope) {
                    case ExecuteSome<T> _e -> {
                        if (i > limit) {
                            break outer;
                        }
                    }
                    case ExecuteAllIgnoreErrors<T> _e -> {
                    }
                }
            }
            scope.join();
            return scope.results();
        } catch (InterruptedException e) {
            throw new ChuuServiceException(e);
        }
    }

    public interface CheckedFunction<J, T> {
        T apply(J item) throws Exception;
    }

    private static sealed abstract class CustomPools<T> extends StructuredTaskScope<T> {
        abstract List<T> results();
    }

    private static final class ExecuteSome<T> extends CustomPools<T> {
        private static final String LOG_PREFIX = "TRACER CustomStructuredTaskScope ";

        private final AtomicInteger successCounter = new AtomicInteger(0);
        private final AtomicBoolean hasReachedThreshold = new AtomicBoolean(false);
        private final List<T> results = new ArrayList<>();
        // sanity check:
        private final AtomicInteger failCounter = new AtomicInteger(0);
        private long numTasksForSuccess = 0;

        public ExecuteSome(long numTasksForSuccess) {
            this.numTasksForSuccess = numTasksForSuccess;
        }

        @Override
        protected void handleComplete(Future<T> future) {
            try {
                var state = future.state();
                if (state == Future.State.SUCCESS) {
                    int numSuccess = successCounter.incrementAndGet();
                    if (numSuccess <= numTasksForSuccess) {
                        results.add(future.resultNow());
                    }

                    if (numSuccess == numTasksForSuccess) {
                        shutdown();
                    }
                } else if (state == Future.State.FAILED) {
                    failCounter.incrementAndGet();
                }
            } catch (Exception ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        }

        //
        public List<T> results() {
            return results.stream().filter(Objects::nonNull).toList();
        }
    }

    private static final class ExecuteAllIgnoreErrors<T> extends CustomPools<T> {
        private final AtomicInteger forkCount = new AtomicInteger(0);
        private final AtomicInteger preJoinCount = new AtomicInteger(0);

        private final AtomicBoolean isInCollection = new AtomicBoolean(false);

        private final List<T> results = new ArrayList<>();

        private final AtomicInteger failCounter = new AtomicInteger(0);

        public ExecuteAllIgnoreErrors() {
        }

        @Override
        public <U extends T> Future<U> fork(Callable<? extends U> task) {
            forkCount.incrementAndGet();
            forkCount.addAndGet(preJoinCount.get());
            return super.fork(task);
        }

        @Override
        public StructuredTaskScope<T> join() throws InterruptedException {
            isInCollection.compareAndSet(false, true);
            return super.join();
        }

        @Override
        protected void handleComplete(Future<T> future) {
            var state = future.state();
            if (state == Future.State.SUCCESS) {
                results.add(future.resultNow());
            } else if (state == Future.State.FAILED) {
                failCounter.incrementAndGet();
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
            return results.stream().filter(Objects::nonNull).toList();
        }

    }
}
