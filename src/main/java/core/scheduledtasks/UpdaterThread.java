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
import dao.entities.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Thread that is the core of the application
 * Will update the less updated person
 */
public class UpdaterThread implements Runnable {

    private final ChuuService dao;
    private final ConcurrentLastFM lastFM;
    private final Spotify spotify;
    private final boolean isIncremental;
    private final DiscogsApi discogsApi;
    private final Random r = new Random();


    public UpdaterThread(ChuuService dao, boolean isIncremental) {
        this.dao = dao;
        lastFM = LastFMFactory.getNewInstance();
        spotify = SpotifySingleton.getInstance();
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.isIncremental = isIncremental;
    }

    public static List<ScrobbledArtist> groupAlbumsToArtist(List<ScrobbledAlbum> scrobbledAlbums) {
        Map<ScrobbledArtist, Integer> a = new HashMap<>();
        for (ScrobbledArtist x : scrobbledAlbums) {
            a.merge(new ScrobbledArtist(x.getArtist(), 0, null), x.getCount(), Integer::sum);
        }
        return a.entrySet().stream().map(x -> {
            ScrobbledArtist key = x.getKey();
            key.setCount(x.getValue());
            return key;
        }).collect(Collectors.toList());
    }

    @Override
    public void run() {
        try {
            System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
            UpdaterUserWrapper userWork;
            float chance = r.nextFloat();
            userWork = dao.getLessUpdated();

            try {
                if (isIncremental && chance <= 0.01f) {

                    TimestampWrapper<List<ScrobbledAlbum>> albumDataList = lastFM
                            .getNewWhole(userWork.getLastFMName(),
                                    userWork.getTimestamp());


                    // Correction with current last fm implementation should return the same name so
                    // no correction gives
                    List<ScrobbledAlbum> albumData = albumDataList.getWrapped();
                    List<ScrobbledArtist> artistData = groupAlbumsToArtist(albumData);
                    albumData = albumData.stream().filter(x -> x != null && !x.getAlbum().isBlank()).collect(Collectors.toList());
                    Map<String, ScrobbledArtist> changedName = new HashMap<>();
                    for (Iterator<ScrobbledArtist> iterator = artistData.iterator(); iterator.hasNext(); ) {
                        ScrobbledArtist datum = iterator.next();
                        try {
                            String artist = datum.getArtist();
                            CommandUtil.validate(dao, datum, lastFM, discogsApi, spotify);
                            String newArtist = datum.getArtist();
                            if (!artist.equals(newArtist)) {
                                ScrobbledArtist e = new ScrobbledArtist(newArtist, 0, null);
                                e.setArtistId(datum.getArtistId());
                                changedName.put(artist, e);
                            }
                        } catch (LastFmEntityNotFoundException ex) {
                            Chuu.getLogger().error("WTF ARTIST DELETED" + datum.getArtist());
                            iterator.remove();
                        }
                    }

                    albumData.forEach(x -> {
                        ScrobbledArtist scrobbledArtist = changedName.get(x.getArtist());
                        if (scrobbledArtist != null) {
                            x.setArtist(scrobbledArtist.getArtist());
                            x.setArtistId(scrobbledArtist.getArtistId());
                        }
                    });

                    dao.incrementalUpdate(new TimestampWrapper<>(artistData, albumDataList.getTimestamp()), userWork.getLastFMName(), albumData);

                    System.out.println("Updated Info Incrementally of " + userWork.getLastFMName()
                            + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
                    System.out.println(" Number of rows updated :" + albumData.size());

                } else {

                    List<ScrobbledAlbum> albumData = lastFM.getALlAlbums(userWork.getLastFMName(), TimeFrameEnum.ALL.toApiFormat());
                    List<ScrobbledArtist> artistData = groupAlbumsToArtist(albumData);

                    dao.albumUpdate(albumData, artistData, userWork.getLastFMName());
                    System.out.println("Updated Info Normally  of " + userWork.getLastFMName()
                            + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
                    System.out.println(" Number of rows updated :" + albumData.size());
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