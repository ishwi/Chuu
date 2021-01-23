package core.services;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TrackWithArtistId;
import dao.entities.UpdaterUserWrapper;
import dao.exceptions.InstanceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NPService {
    private final ConcurrentLastFM lastFM;
    private final LastFMData user;
    private final ChuuService db;

    public NPService(ConcurrentLastFM lastFM, LastFMData user) throws InstanceNotFoundException {
        this.lastFM = lastFM;
        this.user = user;
        if (user.getName() == null) {
            throw new InstanceNotFoundException(user.getDiscordId());
        }
        this.db = Chuu.getDao();
    }

    public NPUpdate getNowPlayingBoth() throws InstanceNotFoundException, LastFmException {
        UpdaterUserWrapper wrapper = db.getUserUpdateStatus(user.getDiscordId());
        if (user.isPrivateUpdate()) {
            return new NPUpdate(lastFM.getNowPlayingInfo(user), CompletableFuture.completedFuture((new ArrayList<>())));
        }

        NPUpdate npWithUpdate = lastFM.getNPWithUpdate(user, wrapper.getTimestamp(), true);
        boolean removeFlag = true;
        try {
            if (!UpdaterService.lockAndContinue(wrapper.getLastFMName())) {
                removeFlag = false;
                Chuu.getLogger().warn("User was being updated while querying for NP " + user.getName());
                npWithUpdate.data.cancel(true);
            } else {
                npWithUpdate.data().thenAccept(list -> new UpdaterHoarder(wrapper, db, lastFM).updateList(list));
            }
            return npWithUpdate;
        } finally {
            if (removeFlag) {
                UpdaterService.remove(wrapper.getLastFMName());
            }
        }
    }

    public NowPlayingArtist getNowPlaying() throws InstanceNotFoundException, LastFmException {

        return getNowPlayingBoth().np;
    }

    public record NPUpdate(NowPlayingArtist np, CompletableFuture<List<TrackWithArtistId>> data) {
    }

}
