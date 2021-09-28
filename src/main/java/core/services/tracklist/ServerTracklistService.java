package core.services.tracklist;

import dao.ChuuService;
import dao.entities.FullAlbumEntity;

import java.util.Optional;

public class ServerTracklistService extends TracklistService {
    private final long guildId;

    public ServerTracklistService(ChuuService service, long guildId) {
        super(service);
        this.guildId = guildId;
    }


    @Override
    protected Optional<FullAlbumEntity> obtainTrackList(long albumId) {
        return service.getServerAlbumTrackList(albumId, guildId);
    }
}
