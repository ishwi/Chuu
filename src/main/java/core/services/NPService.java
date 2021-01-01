package core.services;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.UpdaterUserWrapper;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;

public class NPService {
    private final ConcurrentLastFM lastFM;
    private final LastFMData user;
    private final ChuuService db;

    public NPService(ConcurrentLastFM lastFM, LastFMData user) {
        this.lastFM = lastFM;
        this.user = user;
        this.db = Chuu.getDao();
    }

    public NowPlayingArtist getNowPlaying() throws InstanceNotFoundException {

        UpdaterUserWrapper wrapper = db.getUserUpdateStatus(user.getDiscordId());


        new UsersWrapper(user.getDiscordId(), user.getName(), wrapper.getTimestamp(), )
        UpdaterHoarder hoarder = new UpdaterHoarder(wrapper, db, lastFM);
        hoarder.updateUser();
        updaterService
    }
}
