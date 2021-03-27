package core.scheduledtasks;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.UsersWrapper;

import java.util.List;

public class ArtistMbidUpdater implements Runnable {
    private final ChuuService dao;
    private final ConcurrentLastFM lastFM;

    public ArtistMbidUpdater(ChuuService dao) {
        this.dao = dao;
        lastFM = LastFMFactory.getNewInstance();
    }

    public ArtistMbidUpdater(ChuuService dao, ConcurrentLastFM api) {
        this.dao = dao;
        lastFM = api;
    }

    @Override
    public void run() {
        UsersWrapper randomUser = dao.getRandomUser();
        Chuu.getLogger().info("Searching for MBIDS :)");
        updateAndGet(LastFMData.ofUserWrapper(randomUser));
    }

    public List<ScrobbledArtist> updateAndGet(LastFMData user) {

        try {
            List<ScrobbledArtist> artistData = lastFM.getAllArtists(user, new CustomTimeFrame(TimeFrameEnum.ALL)).stream().
                    filter(t -> t.getArtistMbid() != null && !t.getArtistMbid().isBlank()).toList();
            dao.updateMbids(artistData);
            return artistData;
        } catch (Exception exception) {
            Chuu.getLogger().warn(exception.getMessage(), exception);
        }
        return null;
    }
}
