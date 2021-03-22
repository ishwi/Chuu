package core.services.tracklist;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.exceptions.LastFmException;
import core.services.TagAlbumService;
import core.services.TagArtistService;
import core.services.TrackTagService;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.NowPlayingArtist;
import dao.entities.TrackInfo;

import java.util.List;
import java.util.concurrent.ExecutorService;

public final class TagStorer {
    private final ChuuService db;
    private final ConcurrentLastFM lastFM;
    private final ExecutorService executor;
    private final NowPlayingArtist nowPlayingInfo;

    public TagStorer(ChuuService db, ConcurrentLastFM lastFM, ExecutorService executor,
                     NowPlayingArtist nowPlayingInfo) {
        this.db = db;
        this.lastFM = lastFM;
        this.executor = executor;
        this.nowPlayingInfo = nowPlayingInfo;
    }

    public List<String> findTags() throws LastFmException {
        List<String> tags = lastFM.getTrackTags(1, TopEntity.TRACK, nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName());
        if (tags.isEmpty()) {
            tags = lastFM.getTrackTags(1, TopEntity.ALBUM, nowPlayingInfo.getArtistName(), nowPlayingInfo.getAlbumName());
        } else {
            executor.submit(new TrackTagService(db, lastFM, tags, new TrackInfo(nowPlayingInfo.getArtistName(), null, nowPlayingInfo.getSongName(), null)));
        }
        if (tags.isEmpty()) {
            tags = lastFM.getTrackTags(1, TopEntity.ARTIST, nowPlayingInfo.getArtistName(), null);
        } else {
            if (nowPlayingInfo.getAlbumName() != null && !nowPlayingInfo.getAlbumName().isBlank())
                executor.submit(new TagAlbumService(db, lastFM, tags, new AlbumInfo(nowPlayingInfo.getAlbumMbid(), nowPlayingInfo.getAlbumName(), nowPlayingInfo.getArtistName())));
        }
        if (!tags.isEmpty()) {
            executor.submit(new TagArtistService(db, lastFM, tags, new ArtistInfo(nowPlayingInfo.getUrl(), nowPlayingInfo.getArtistName(), nowPlayingInfo.getArtistMbid())));
        }
        return tags;
    }


}
