package core.scheduledtasks;

import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.UnknownLastFmException;
import core.parsers.utils.CustomTimeFrame;
import core.services.UpdaterHoarder;
import core.services.UpdaterService;
import dao.ChuuService;
import dao.entities.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Thread that is the core of the application
 * Will update the less updated person
 */
public class UpdaterThread implements Runnable {

    private final ChuuService dao;
    private final ConcurrentLastFM lastFM;
    private final boolean isIncremental;
    private final Random r = new Random();


    public UpdaterThread(ChuuService dao, boolean isIncremental) {
        this.dao = dao;
        lastFM = LastFMFactory.getNewInstance();
        this.isIncremental = isIncremental;
    }

    public static List<ScrobbledArtist> groupAlbumsToArtist(List<ScrobbledAlbum> scrobbledAlbums) {
        Map<ScrobbledArtist, Integer> a = new HashMap<>();
        for (ScrobbledArtist x : scrobbledAlbums) {
            ScrobbledArtist key = new ScrobbledArtist(x.getArtist(), 0, null);
            key.setArtistId(x.getArtistId());
            a.merge(key, x.getCount(), Integer::sum);
        }
        return a.entrySet().stream().map(x -> {
            ScrobbledArtist key = x.getKey();
            key.setCount(x.getValue());
            return key;
        }).toList();
    }

    @Override
    public void run() {
        try {
            System.out.println("THREAD WORKING ) + " + LocalDateTime.now());
            UpdaterUserWrapper userWork;

            float chance = r.nextFloat();
            userWork = dao.getLessUpdated();
            boolean removeFlag = true;
            try {
                if (!UpdaterService.lockAndContinue(userWork.getLastFMName())) {
                    removeFlag = false;
                    Chuu.getLogger().warn("User was being updated" + userWork.getLastFMName());
                    return;
                }

                String lastFMName = userWork.getLastFMName();
                try {
                    if (isIncremental && chance <= 0.995f) {
                        UpdaterHoarder updaterHoarder = new UpdaterHoarder(userWork, dao, lastFM, dao.findLastFMData(userWork.getDiscordID()));
                        updaterHoarder.updateUser();
                        System.out.println("Updated Info Incrementally of " + lastFMName
                                           + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));

                    } else {

                        List<ScrobbledArtist> artistData = lastFM.getAllArtists(LastFMData.ofUserWrapper(userWork), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                        dao.insertArtistDataList(artistData, lastFMName);
                        System.out.println(" Updated  ) " + lastFMName + " artists");

                        List<ScrobbledAlbum> albumData = lastFM.getAllAlbums(LastFMData.ofUserWrapper(userWork), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                        dao.albumUpdate(albumData, artistData, lastFMName);
                        System.out.println(" Updated  ) " + lastFMName + " albums");
                        List<ScrobbledTrack> trackData = lastFM.getAllTracks(LastFMData.ofUserWrapper(userWork), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
                        dao.trackUpdate(trackData, artistData, lastFMName);
                        System.out.println(" Updated  ) " + lastFMName + " tracks");

                    }
                } catch (LastFMNoPlaysException e) {
                    // dao.updateUserControlTimestamp(userWork.getLastFMName(),userWork.getTimestampControl());
                    dao.updateUserTimeStamp(lastFMName, userWork.getTimestamp(),
                            (int) (Instant.now().getEpochSecond() + 4000));
                    System.out.println("No plays " + lastFMName
                                       + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
                } catch (LastFmEntityNotFoundException e) {
                    dao.removeUserCompletely(userWork.getDiscordID());
                } catch (UnknownLastFmException ex) {
                    int code = ex.getCode();
                    if (code == 17) {
                        dao.updateUserTimeStamp(lastFMName, userWork.getTimestamp(),
                                (int) (Instant.now().getEpochSecond() + 10000));
                        Chuu.getLogger().warn("User {} code 17 while updateing ", lastFMName);
                    } else {
                        System.out.println("Error while updating" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
                        Chuu.getLogger().warn(ex.getMessage(), ex);
                    }

                }
            } finally {
                if (removeFlag) {
                    UpdaterService.remove(userWork.getLastFMName());
                }
            }
        } catch (Throwable e) {
            System.out.println("Error while updating" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
            Chuu.getLogger().warn(e.getMessage(), e);
        }

    }
}
