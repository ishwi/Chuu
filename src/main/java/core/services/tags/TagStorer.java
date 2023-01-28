package core.services.tags;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.NowPlayingArtist;
import dao.entities.TrackInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public record TagStorer(ChuuService db, ConcurrentLastFM lastFM,
                        ExecutorService executor, NowPlayingArtist nowPlayingInfo) {

    public List<String> findTags() throws LastFmException {
        return findTags(5);
    }

    public List<String> findTags(int limit) {
        Set<String> tags;
        if ((CommandUtil.rand.nextFloat() < 0.8)) {
            tags = getStrings(limit, () -> getArtistTags(limit), () -> getTrackTags(limit));
        } else {
            tags = getStrings(limit, () -> getTrackTags(limit), () -> getArtistTags(limit));
        }
        return new ArrayList<>(tags);
    }

    @NotNull
    private Set<String> getStrings(int limit, Supplier<List<String>> first, Supplier<List<String>> fallback) {
        Set<String> tags;
        tags = new HashSet<>(first.get());
        if (tags.isEmpty() || tags.size() < limit) {
            tags.addAll(fallback.get());
            if (tags.isEmpty() || tags.size() < limit) {
                tags.addAll(getAlbumTags(limit));
            }
        }
        return tags;
    }

    private List<String> getArtistTags(int limit) {
        List<String> tags;
        try {
            tags = lastFM.getTrackTags(limit, TopEntity.ARTIST, nowPlayingInfo.artistName(), null);
        } catch (LastFmException e) {
            return Collections.emptyList();
        }
        if (!tags.isEmpty()) {
            executor.submit(new TagArtistService(db, lastFM, tags, new ArtistInfo(nowPlayingInfo.url(), nowPlayingInfo.artistName(), nowPlayingInfo.artistMbid())));
        }
        return tags;
    }

    private List<String> getAlbumTags(int limit) {
        if (nowPlayingInfo.albumName() != null && !nowPlayingInfo.albumName().isBlank()) {
            List<String> tags;
            try {
                tags = lastFM.getTrackTags(limit, TopEntity.ALBUM, nowPlayingInfo.artistName(), nowPlayingInfo.albumName());
            } catch (LastFmException e) {
                return Collections.emptyList();
            }
            if (!tags.isEmpty()) {
                executor.submit(new TagAlbumService(db, lastFM, tags, new AlbumInfo(null, nowPlayingInfo.albumName(), nowPlayingInfo.artistName())));
            }
            return tags;
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> getTrackTags(int limit) {
        List<String> tags;
        try {
            tags = lastFM.getTrackTags(limit, TopEntity.TRACK, nowPlayingInfo.artistName(), nowPlayingInfo.songName());
        } catch (LastFmException e) {
            return Collections.emptyList();
        }
        if (!tags.isEmpty()) {
            executor.submit(new TrackTagService(db, lastFM, tags, new TrackInfo(nowPlayingInfo.artistName(), null, nowPlayingInfo.songName(), null)));
        }
        return tags;
    }


}
