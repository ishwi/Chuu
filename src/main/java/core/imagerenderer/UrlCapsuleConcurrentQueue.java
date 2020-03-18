package core.imagerenderer;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.spotify.Spotify;
import core.commands.CommandUtil;
import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import dao.entities.UrlCapsule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class UrlCapsuleConcurrentQueue extends LinkedBlockingQueue<UrlCapsule> {
    private static final long serialVersionUID = 1L;
    private transient final ChuuService dao;
    private transient final DiscogsApi discogsApi;
    private transient final Spotify spotifyApi;
    private transient final LinkedBlockingQueue<CompletableFuture<UrlCapsule>> wrapper;

    public UrlCapsuleConcurrentQueue(ChuuService dao, DiscogsApi discogsApi, Spotify spotify) {
        super();
        this.dao = dao;
        this.discogsApi = discogsApi;
        this.spotifyApi = spotify;
        this.wrapper = new LinkedBlockingQueue<>();
    }

    @Override
    public int size() {
        return this.wrapper.size();
    }

    public boolean offer(@NotNull UrlCapsule item) {
        CompletableFuture<UrlCapsule> future = CompletableFuture.supplyAsync(() -> {
            item.setUrl(null);
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
                e.printStackTrace();
            }
            return item;
        }).toCompletableFuture();
        return wrapper.offer(future);

    }

    @NotNull
    public UrlCapsule take() throws InterruptedException {
        try {
            return wrapper.take().get();
        } catch (ExecutionException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new InterruptedException();
        }
    }

}
