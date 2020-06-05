package core.apis.last.queues;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.PreComputedChartEntity;
import core.apis.spotify.Spotify;
import core.commands.CommandUtil;
import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscardableQueue implements BlockingQueue<UrlCapsule> {
    final boolean needsImages;
    private final int maxNumberOfElements;
    private final LinkedBlockingQueue<UrlCapsule> innerQueue;
    private final ChuuService dao;
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;
    private final Function<UrlCapsule, PreComputedChartEntity> factoryFunction;
    private final Predicate<PreComputedChartEntity> discard;
    protected LinkedBlockingQueue<CompletableFuture<?>> taskQueue;

    public DiscardableQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, Predicate<PreComputedChartEntity> discard, Function<UrlCapsule, PreComputedChartEntity> factoryFunction, int maxNumberOfElements) {
        this(maxNumberOfElements, dao, discogsApi, spotify, factoryFunction, discard, true);
    }

    public DiscardableQueue(int maxNumberOfElements, ChuuService dao, DiscogsApi discogsApi, Spotify spotify, Function<UrlCapsule, PreComputedChartEntity> factoryFunction, Predicate<PreComputedChartEntity> discard, boolean needsImages) {
        super();
        innerQueue = new LinkedBlockingQueue<>();
        this.taskQueue = new LinkedBlockingQueue<>();
        this.maxNumberOfElements = maxNumberOfElements;
        this.dao = dao;
        this.discogsApi = discogsApi;
        this.spotifyApi = spotify;
        this.factoryFunction = factoryFunction;
        this.discard = discard;
        this.needsImages = needsImages;

    }

    @Override
    public boolean offer(@NotNull UrlCapsule item) {
        CompletableFuture<?> future = CompletableFuture.supplyAsync(() -> {
            if (innerQueue.size() < maxNumberOfElements) {

                if (item.getUrl() == null) {
                    getUrl(item);
                }
                if (item.getUrl() != null) {
                    PreComputedChartEntity entity = factoryFunction.apply(item);
                    if (!discard.test(entity)) {
                        innerQueue.add(entity);
                    } else if (entity.getImage() != null) {
                        entity.getImage().flush();
                    }
                }
            }
            return 0;
        }).toCompletableFuture();
        return taskQueue.offer(future);
    }

    private void getUrl(@NotNull UrlCapsule item) {
        try {
            UpdaterStatus updaterStatusByName = dao.getUpdaterStatusByName(item.getArtistName());
            String url = updaterStatusByName.getArtistUrl();
            if (url == null) {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(item.getArtistName(), item.getPlays(), item.getUrl());
                scrobbledArtist.setArtistId(updaterStatusByName.getArtistId());
                url = CommandUtil.updateUrl(discogsApi, scrobbledArtist, dao, spotifyApi);
            }
            item.setUrl(url);
        } catch (InstanceNotFoundException e) {
            //What can we do
        }
    }

    @Override
    public int drainTo(@NotNull Collection<? super UrlCapsule> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        int counter = 0;
        for (CompletableFuture<?> urlCapsuleCompletableFuture : taskQueue) {
            try {
                urlCapsuleCompletableFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Chuu.getLogger().warn("Future stopped", e);
            }
        }
        innerQueue.drainTo(c);
        return counter;
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
    public <T> T[] toArray(@NotNull T[] a) {
        return innerQueue.toArray(a);
    }

    @Override
    public int drainTo(@NotNull Collection<? super UrlCapsule> c) {
        return this.drainTo(c, Integer.MAX_VALUE);
    }
}
