package test.apis.last;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFMServiceException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import dao.entities.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConcurrentLastFMTest {
    private final String nonPlaysOnUser = "test3";

    @Test
    public void getAlbumsFromArtist() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

        try {
            ArtistAlbums artistAlbums = lastFM.getAlbumsFromArtist("cher", 10);
            assertTrue("cher".equalsIgnoreCase(artistAlbums.getArtist()));
            assertEquals(10, artistAlbums.getAlbumList().size());

            artistAlbums = lastFM.getAlbumsFromArtist("cherr", 10);
            assertTrue("cher".equalsIgnoreCase(artistAlbums.getArtist()));
            assertEquals(10, artistAlbums.getAlbumList().size());


        } catch (LastFMServiceException ignored) {
        }


    }

    @Test(expected = LastFmEntityNotFoundException.class)
    public void EntityNotFound() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

        try {
            SecondsTimeFrameCount secondsTimeFrameCount = lastFM.getMinutesWastedOnMusic("a8zm219xm1-09cu-", "7day");
            int seconds = secondsTimeFrameCount.getSeconds();
            int count = secondsTimeFrameCount.getCount();

        } catch (LastFMServiceException ignored) {
        }


    }

    @Test
    public void getSeconds() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

        try {
            SecondsTimeFrameCount secondsTimeFrameCount = lastFM.getMinutesWastedOnMusic("lukyfan", "7day");
            int seconds = secondsTimeFrameCount.getSeconds();
            int count = secondsTimeFrameCount.getCount();

        } catch (LastFMServiceException ignored) {
        }


    }

    @Test
    public void getTopArtistTracks() {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

        try {
            List<Track> secondsTimeFrameCount = lastFM.getTopArtistTracks("lukyfan", "Northlane", "7day", "Northlane");

        } catch (LastFmException ignored) {
        }
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void getIncrementalNonPLaysUser() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getWhole(nonPlaysOnUser, 0);
    }

    @Test(expected = LastFmEntityNotFoundException.class)
    public void getIncrementalNonExistingUser() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getWhole("iausdhiaushdnbiuasnbdiuasnbdiua", 0);
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void empty() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        TimestampWrapper<List<ScrobbledArtist>> ishwaracoello = lastFM
                .getWhole("ishwaracoello", (int) (Instant.now().getEpochSecond() + 4000));
    }

    //Will fail if I stop listening to music for 11 days
    @Test
    public void nonempty() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        TimestampWrapper<List<ScrobbledArtist>> ishwaracoello = lastFM
                .getWhole("ishwaracoello", (int) (Instant.now().getEpochSecond() - 1000000));
        Assert.assertFalse(ishwaracoello.getWrapped().isEmpty());
    }

    @Test
    public void notExistingArtist() throws LastFmException {

        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        assertEquals(lastFM.getCorrection("aiusdhbniaubnscinabsiuc"), "aiusdhbniaubnscinabsiuc");

    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions5() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getListTopTrack(nonPlaysOnUser, TimeFrameEnum.WEEK.toApiFormat());
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions4() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getNowPlayingInfo(nonPlaysOnUser);
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions3() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getTracksAndTimestamps(nonPlaysOnUser, (int) Instant.now().getEpochSecond(), (int) Instant.now()
                .getEpochSecond() + 3);
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions2() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getMinutesWastedOnMusicDaily(nonPlaysOnUser, null, (int) Instant.now().getEpochSecond());
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions1() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        //lastFM.getChart(nonPlaysOnUser, TimeFrameEnum.WEEK.toApiFormat(), 1, 1, true, null);
    }


}
