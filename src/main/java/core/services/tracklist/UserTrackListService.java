package core.services.tracklist;

import dao.ChuuService;
import dao.entities.FullAlbumEntity;

import java.util.Optional;

public class UserTrackListService extends TracklistService {
    private final String lastfmId;

    public UserTrackListService(ChuuService service, String lastfmId) {
        super(service);
        this.lastfmId = lastfmId;
    }

    @Override
    protected Optional<FullAlbumEntity> obtainTrackList(long albumId) {
        return service.getAlbumTrackList(albumId, lastfmId);
    }


}
