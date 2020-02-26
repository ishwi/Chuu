package core.scheduledtasks;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.CommandUtil;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.TimestampWrapper;
import dao.entities.UpdaterUserWrapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Thread that is the core of the appliaction
 * Will update the less updated person
 */
public class UpdaterThread implements Runnable {

    private final ChuuService dao;
    private final ConcurrentLastFM lastFM;
    private final Spotify spotify;
    private final boolean isIncremental;
    private final DiscogsApi discogsApi;


    public UpdaterThread(ChuuService dao, boolean isIncremental) {
        this.dao = dao;
        lastFM = LastFMFactory.getNewInstance();
        spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.isIncremental = isIncremental;
    }

    @Override
    public void run() {
        try {
            System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
            UpdaterUserWrapper userWork;
            Random r = new Random();
            float chance = r.nextFloat();
            userWork = dao.getLessUpdated();

            try {
                if (isIncremental && chance <= 0.93f) {

                    TimestampWrapper<List<ScrobbledArtist>> artistDataLinkedList = lastFM
                            .getWhole(userWork.getLastFMName(),
                                    userWork.getTimestamp());

                    // Correction with current last fm implementation should return the same name so
                    // no correction gives
                    for (Iterator<ScrobbledArtist> iterator = artistDataLinkedList.getWrapped().iterator(); iterator.hasNext(); ) {
                        ScrobbledArtist datum = iterator.next();
                        try {
                            CommandUtil.validate(dao, datum, lastFM, discogsApi, spotify);
                        } catch (LastFmEntityNotFoundException ex) {
                            Chuu.getLogger().error("WTF ARTIST DELETED" + datum.getArtist());
                            iterator.remove();
                        }
                    }

                    dao.incrementalUpdate(artistDataLinkedList, userWork.getLastFMName());

                    System.out.println("Updated Info Incrementally of " + userWork.getLastFMName()
                            + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
                    System.out.println(" Number of rows updated :" + artistDataLinkedList.getWrapped().size());
                } else {

                    List<ScrobbledArtist> scrobbledArtistLinkedList = lastFM.getAllArtists(userWork.getLastFMName(), TimeFrameEnum.ALL.toApiFormat());
                    dao.insertArtistDataList(scrobbledArtistLinkedList, userWork.getLastFMName());
                    System.out.println("Updated Info Normally  of " + userWork.getLastFMName()
                            + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
                    System.out.println(" Number of rows updated :" + scrobbledArtistLinkedList.size());
                }
            } catch (LastFMNoPlaysException e) {
                // dao.updateUserControlTimestamp(userWork.getLastFMName(),userWork.getTimestampControl());
                dao.updateUserTimeStamp(userWork.getLastFMName(), userWork.getTimestamp(),
                        (int) (Instant.now().getEpochSecond() + 4000));
                System.out.println("No plays " + userWork.getLastFMName()
                        + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
            } catch (LastFmEntityNotFoundException e) {
                dao.removeUserCompletely(userWork.getDiscordID());
            }

        } catch (Throwable e) {
            System.out.println("Error while updating" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
            Chuu.getLogger().warn(e.getMessage(), e);
        }
    }
}