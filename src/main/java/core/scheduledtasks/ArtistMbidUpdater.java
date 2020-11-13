package core.scheduledtasks;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
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

    @Override
    public void run() {
        UsersWrapper randomUser = dao.getRandomUser();
        String lastFMName = randomUser.getLastFMName();
        System.out.println("SEARCHING FOR MBIDS");

        try {
            List<ScrobbledArtist> artistData = lastFM.getAllArtists(lastFMName, new CustomTimeFrame(TimeFrameEnum.ALL));
            dao.updateMbids(artistData);
        } catch (Exception exception) {
            Chuu.getLogger().warn(exception.getMessage(), exception);
        }
    }
}
