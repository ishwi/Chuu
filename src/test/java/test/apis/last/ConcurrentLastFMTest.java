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
            SecondsTimeFrameCount secondsTimeFrameCount = lastFM.getMinutesWastedOnMusic(LastFMData.ofUser("a8zm219xm1-09cu-"), TimeFrameEnum.WEEK);
            int seconds = secondsTimeFrameCount.getSeconds();
            int count = secondsTimeFrameCount.getCount();

        } catch (LastFMServiceException ignored) {
        }


    }

    @Test
    public void getSeconds() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

        try {
            SecondsTimeFrameCount secondsTimeFrameCount = lastFM.getMinutesWastedOnMusic(LastFMData.ofUser("lukyfan"), TimeFrameEnum.WEEK);
            int seconds = secondsTimeFrameCount.getSeconds();
            int count = secondsTimeFrameCount.getCount();

        } catch (LastFMServiceException ignored) {
        }


    }

    @Test
    public void getTopArtistTracks() {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

        try {
            List<Track> secondsTimeFrameCount = lastFM.getTopArtistTracks(LastFMData.ofUser("lukyfan"), "Northlane", TimeFrameEnum.WEEK, "Northlane");

        } catch (LastFmException ignored) {
        }
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void getIncrementalNonPLaysUser() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getWeeklyBillboard(LastFMData.ofUser(nonPlaysOnUser), 0, Integer.MAX_VALUE);
    }

    @Test(expected = LastFmEntityNotFoundException.class)
    public void getIncrementalNonExistingUser() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getWeeklyBillboard(LastFMData.ofUser("iausdhiaushdnbiuasnbdiuasnbdiua"), 0, Integer.MAX_VALUE);

    }

    @Test(expected = LastFMNoPlaysException.class)
    public void empty() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        var ishwaracoello = lastFM
                .getWeeklyBillboard(LastFMData.ofUser("ishwaracoello"), (int) (Instant.now().getEpochSecond() + 4000), Integer.MAX_VALUE);
    }

    //Will fail if I stop listening to music for 11 days
    @Test
    public void nonempty() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        var ishwaracoello = lastFM
                .getWeeklyBillboard(LastFMData.ofUser("ishwaracoello"), (int) (Instant.now().getEpochSecond() - 1000000), Integer.MAX_VALUE);
        Assert.assertFalse(ishwaracoello.isEmpty());
    }

    @Test
    public void notExistingArtist() throws LastFmException {

        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        assertEquals(lastFM.getCorrection("aiusdhbniaubnscinabsiuc"), "aiusdhbniaubnscinabsiuc");

    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions5() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getListTopTrack(LastFMData.ofUser(nonPlaysOnUser), TimeFrameEnum.WEEK);
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions4() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getNowPlayingInfo(LastFMData.ofUser(nonPlaysOnUser));
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions3() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getTracksAndTimestamps(LastFMData.ofUser(nonPlaysOnUser), (int) Instant.now().getEpochSecond(), (int) Instant.now()
                .getEpochSecond() + 3);
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions2() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        lastFM.getMinutesWastedOnMusicDaily(LastFMData.ofUser(nonPlaysOnUser), null, (int) Instant.now().getEpochSecond());
    }

    @Test(expected = LastFMNoPlaysException.class)
    public void noPlaysExceptions1() throws LastFmException {
        ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
        //lastFM.getChart(nonPlaysOnUser, TimeFrameEnum.WEEK.toApiFormat(), 1, 1, true, null);
    }


}
