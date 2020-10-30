package core.apis.last.queues;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.spotify.Spotify;
import core.commands.CommandUtil;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import dao.exceptions.InstanceNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import static core.apis.last.queues.TrackGroupAlbumQueue.defaultTrackImage;

public class ArtistQueue extends LinkedBlockingQueue<UrlCapsule> {
    private static final long serialVersionUID = 1L;
    protected final transient LinkedBlockingQueue<CompletableFuture<UrlCapsule>> wrapper;
    private final transient ChuuService dao;
    private final transient DiscogsApi discogsApi;
    private final transient Spotify spotifyApi;
    private final boolean needsImages;

    public ArtistQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify) {
        this(dao, discogsApi, spotify, true);
    }

    public ArtistQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify, boolean needsImages) {
        super();
        this.dao = dao;
        this.discogsApi = discogsApi;
        this.spotifyApi = spotify;
        this.needsImages = needsImages;
        this.wrapper = new LinkedBlockingQueue<>();
    }

    @Override
    public int size() {
        return this.wrapper.size();
    }

    @Override
    public boolean offer(@NotNull UrlCapsule item) {
        CompletableFuture<UrlCapsule> future = CompletableFuture.supplyAsync(() -> {
            if (needsImages) {
                if ((item.getUrl() != null && item.getUrl().isBlank()) || (item.getUrl() != null && item.getUrl().equalsIgnoreCase(defaultTrackImage))) {
                    item.setUrl(null);
                }
                getUrl(item);
            }
            return item;
        }).toCompletableFuture();
        return wrapper.offer(future);

    }

    public void getUrl(@NotNull UrlCapsule item) {
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


    @NotNull
    @Override
    public UrlCapsule take() throws InterruptedException {
        try {
            return wrapper.take().get();
        } catch (ExecutionException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new InterruptedException();
        }
    }

    @Override
    public int drainTo(Collection<? super UrlCapsule> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        int counter = 0;
        for (CompletableFuture<UrlCapsule> urlCapsuleCompletableFuture : wrapper) {
            if (counter == maxElements) {
                break;
            }
            try {
                c.add(urlCapsuleCompletableFuture.get());

                counter++;
            } catch (InterruptedException | ExecutionException e) {
                Chuu.getLogger().warn("Future stopped", e);
            }
        }
        return counter;
    }
}
