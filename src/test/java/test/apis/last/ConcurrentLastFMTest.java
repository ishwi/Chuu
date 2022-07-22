package test.apis.last;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFMServiceException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import dao.entities.ArtistAlbums;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

public class ConcurrentLastFMTest {
    private static ConcurrentLastFM lastFM;
    private final String nonPlaysOnUser = "test3";

    @BeforeAll
    public static void init() {
        lastFM = LastFMFactory.getNewInstance();
    }

    @Test
    public void getAlbumsFromArtist() throws LastFmException {

        try {
            ArtistAlbums artistAlbums = lastFM.getAlbumsFromArtist("cher", 10);
            assertThat("cher".equalsIgnoreCase(artistAlbums.getArtist())).isTrue();
            assertThat(artistAlbums.getAlbumList()).hasSize(10);

            artistAlbums = lastFM.getAlbumsFromArtist("cherr", 10);
            assertThat("cher".equalsIgnoreCase(artistAlbums.getArtist())).isTrue();
            assertThat(artistAlbums.getAlbumList()).hasSize(10);


        } catch (LastFMServiceException ignored) {
        }


    }

    @Test
    public void EntityNotFound() {
        assertThatThrownBy(() -> lastFM.getMinutesWastedOnMusic(LastFMData.ofUser("a8zm219xm1-09cu-"), TimeFrameEnum.WEEK))
                .isInstanceOf(LastFmEntityNotFoundException.class);


    }

    @Test
    public void getSeconds() {
        assertThatCode(() -> lastFM.getMinutesWastedOnMusic(LastFMData.ofUser("lukyfan"), TimeFrameEnum.WEEK)).doesNotThrowAnyException();


    }

    @Test
    public void getTopArtistTracks() {
        assertThatCode(() -> lastFM.getTopArtistTracks(LastFMData.ofUser("lukyfan"), "Northlane", TimeFrameEnum.WEEK, "Northlane")).doesNotThrowAnyException();
    }

    @Test()
    public void getIncrementalNonPLaysUser() {
        LastFMData user = LastFMData.ofUser(nonPlaysOnUser);
        assertThatThrownBy(() -> lastFM.getWeeklyBillboard(user, 0, Integer.MAX_VALUE)).isInstanceOf(LastFMNoPlaysException.class);

    }

    @Test()
    public void getIncrementalNonExistingUser() {
        LastFMData user = LastFMData.ofUser("iausdhiaushdnbiuasnbdiuasnbdiua");
        assertThatThrownBy(() -> lastFM.getWeeklyBillboard(user, 0, Integer.MAX_VALUE)).isInstanceOf(LastFmEntityNotFoundException.class);

    }

    @Test()
    public void empty() {
        LastFMData user = LastFMData.ofUser("ishwaracoello");
        assertThatThrownBy(() -> lastFM
                .getWeeklyBillboard(user, (int) (Instant.now().getEpochSecond() + 4000), Integer.MAX_VALUE)).isInstanceOf(LastFMNoPlaysException.class);
    }

    //Will fail if I stop listening to music for 11 days
    @Test
    public void nonempty() throws LastFmException {
        var ishwaracoello = lastFM
                .getWeeklyBillboard(LastFMData.ofUser("ishwaracoello"), (int) (Instant.now().getEpochSecond() - 1000000), Integer.MAX_VALUE);
        assertThat(ishwaracoello.isEmpty()).isFalse();
    }

    @Test
    public void notExistingArtist() throws LastFmException {
        assertThat("aiusdhbniaubnscinabsiuc").isEqualTo(lastFM.getCorrection("aiusdhbniaubnscinabsiuc"));

    }

    @Test
    public void noPlaysExceptions5() {
        LastFMData user = LastFMData.ofUser(nonPlaysOnUser);
        assertThatThrownBy(() -> lastFM.getListTopTrack(user, TimeFrameEnum.WEEK)).isInstanceOf(LastFMNoPlaysException.class);
    }

    @Test()
    public void noPlaysExceptions4() {
        LastFMData user = LastFMData.ofUser(nonPlaysOnUser);
        assertThatThrownBy(() -> lastFM.getNowPlayingInfo(user)).isInstanceOf(LastFMNoPlaysException.class);
    }

    @Test()
    public void noPlaysExceptions3() {
        LastFMData user = LastFMData.ofUser(nonPlaysOnUser);

        assertThatThrownBy(() -> lastFM.getTracksAndTimestamps(user, (int) Instant.now().getEpochSecond(), (int) Instant.now()
                .getEpochSecond() + 3)).isInstanceOf(LastFMNoPlaysException.class);


    }

    @Test()
    public void noPlaysExceptions2() {
        LastFMData user = LastFMData.ofUser(nonPlaysOnUser);
        assertThatThrownBy(() -> lastFM.getMinutesWastedOnMusicDaily(user,
                null, (int) Instant.now().getEpochSecond())).isInstanceOf(LastFMNoPlaysException.class);

    }


}
