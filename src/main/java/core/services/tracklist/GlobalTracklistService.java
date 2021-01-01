package core.services.tracklist;

import dao.ChuuService;
import dao.entities.FullAlbumEntity;

import java.util.Optional;

public class GlobalTracklistService extends TracklistService {

    public GlobalTracklistService(ChuuService service) {
        super(service);
    }

    @Override
    protected Optional<FullAlbumEntity> obtainTrackList(long albumId) {
        return service.getGlobalAlbumTrackList(albumId);
    }


}
