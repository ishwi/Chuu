package core.apis.last.queues;

import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.util.VirtualParallel;
import dao.exceptions.ChuuServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscardableQueue<T extends UrlCapsule> implements BlockingQueue<UrlCapsule> {
    protected final LinkedBlockingQueue<CompletableFuture<T>> taskQueue;
    final boolean needsImages;
    private final int maxNumberOfElements;
    private final LinkedBlockingQueue<UrlCapsule> innerQueue;
    private final Function<UrlCapsule, T> factoryFunction;
    private final Predicate<T> discard;

    public DiscardableQueue(Predicate<T> discard, Function<UrlCapsule, T> factoryFunction, int maxNumberOfElements) {
        this(maxNumberOfElements, factoryFunction, discard, true);
    }

    public DiscardableQueue(int maxNumberOfElements, Function<UrlCapsule, T> factoryFunction, Predicate<T> discard, boolean needsImages) {
        super();
        innerQueue = new LinkedBlockingQueue<>();
        this.taskQueue = new LinkedBlockingQueue<>();
        this.maxNumberOfElements = maxNumberOfElements;

        this.factoryFunction = factoryFunction;
        this.discard = discard;
        this.needsImages = needsImages;

    }

    @Override
    public boolean offer(@NotNull UrlCapsule item) {
        CompletableFuture<T> future = CommandUtil.supplyLog(() -> {
            if (innerQueue.size() < maxNumberOfElements) {

                T entity = factoryFunction.apply(item);
                if (!discard.test(entity)) {
                    innerQueue.add(entity);
                }
                return entity;

            }
            return null;
        });
        return taskQueue.offer(future);
    }


    @Override
    public int drainTo(@NotNull Collection<? super UrlCapsule> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        try (var scope = new VirtualParallel.ExecuteAllIgnoreErrors<>()) {
            for (CompletableFuture<?> urlCapsuleCompletableFuture : taskQueue) {
                scope.fork(urlCapsuleCompletableFuture::get);
            }
            scope.joinUntil(Instant.now().plus(10, ChronoUnit.SECONDS));
        } catch (InterruptedException | TimeoutException e) {
            throw new ChuuServiceException(e);
        }
        innerQueue.drainTo(c);

        return 0;
    }

    @Override
    public int size() {
        return innerQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return innerQueue.isEmpty();
    }

    @Override
    public boolean add(@NotNull UrlCapsule capsule) {
        return this.offer(capsule);
    }

    @Override
    public UrlCapsule remove() {
        return innerQueue.remove();
    }

    @Override
    public UrlCapsule poll() {
        return innerQueue.poll();
    }

    @Override
    public UrlCapsule element() {
        return innerQueue.element();
    }

    @Override
    public UrlCapsule peek() {
        return innerQueue.peek();
    }

    @Override
    public void put(@NotNull UrlCapsule capsule) throws InterruptedException {
        innerQueue.put(capsule);
    }

    @Override
    public boolean offer(UrlCapsule capsule, long timeout, @NotNull TimeUnit unit) {
        throw new UnsupportedOperationException();
    }


    @NotNull
    @Override
    public UrlCapsule take() throws InterruptedException {
        return innerQueue.take();
    }

    @Nullable
    @Override
    public UrlCapsule poll(long timeout, @NotNull TimeUnit unit) {
        return innerQueue.poll();
    }

    @Override
    public int remainingCapacity() {
        return innerQueue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return innerQueue.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return innerQueue.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends UrlCapsule> c) {
        for (UrlCapsule urlCapsule : c) {
            this.offer(urlCapsule);
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return innerQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return innerQueue.retainAll(c);
    }

    @Override
    public void clear() {
        innerQueue.clear();
    }

    @Override
    public boolean contains(Object o) {
        return innerQueue.contains(o);
    }

    @NotNull
    @Override
    public Iterator<UrlCapsule> iterator() {
        return innerQueue.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return innerQueue.toArray();
    }

    @NotNull
    @Override
    public <Z> Z[] toArray(@NotNull Z[] a) {
        return innerQueue.toArray(a);
    }

    @Override
    public int drainTo(@NotNull Collection<? super UrlCapsule> c) {
        return this.drainTo(c, Integer.MAX_VALUE);
    }
}


