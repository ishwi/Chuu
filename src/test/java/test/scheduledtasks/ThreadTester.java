package test.scheduledtasks;

import core.scheduledtasks.ImageUpdaterThread;
import core.scheduledtasks.SpotifyUpdaterThread;
import core.scheduledtasks.UpdaterThread;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import dao.entities.UpdaterUserWrapper;
import dao.exceptions.InstanceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.commands.utils.TestResources;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestResources.class)
public class ThreadTester {
    @Test
    public void testIncremental() {

        UpdaterUserWrapper lessUpdated = TestResources.dao.getLessUpdated();
        UpdaterThread updaterThread = new UpdaterThread(TestResources.dao, true);
        updaterThread.run();
        UpdaterUserWrapper lessUpdated2 = TestResources.dao.getLessUpdated();
        if (lessUpdated2.getLastFMName().equals(lessUpdated.getLastFMName())) {
            assertThat(lessUpdated2.getTimestampControl() > lessUpdated.getTimestampControl()).isFalse();
        } else {
            updaterThread.run();
            lessUpdated2 = TestResources.dao.getLessUpdated();
            assertThat(lessUpdated2.getLastFMName()).isEqualTo(lessUpdated.getLastFMName());
        }


    }

    @Test
    public void testNotIncremental() {

        UpdaterUserWrapper lessUpdated = TestResources.dao.getLessUpdated();
        int timestampControl = lessUpdated.getTimestampControl();
        UpdaterThread updaterThread = new UpdaterThread(TestResources.dao, false);
        //We insert an non existing artist for the user we are going to update
        TestResources.dao.insertArtistDataList(Collections
                .singletonList(new ScrobbledArtist(lessUpdated
                        .getLastFMName(), "Invented unexisting artist", 1000)), lessUpdated
                .getLastFMName());

        updaterThread.run();
        //After we run the thread the things that are not in last.fm should have dissapeared
        assertThat(TestResources.dao
                .getArtistPlays(-1L, lessUpdated.getLastFMName())).isEqualTo(0);

    }

    @Test
    public void discogsImageUpdated() throws InstanceNotFoundException {
        Set<ScrobbledArtist> nullUrls = TestResources.dao.getNullUrls();
        ImageUpdaterThread imageUpdaterThread = new ImageUpdaterThread(TestResources.dao);
        imageUpdaterThread.run();
        for (ScrobbledArtist nullUrl : nullUrls) {
            UpdaterStatus updaterStatus = TestResources.dao.getUpdaterStatusByName(nullUrl.getArtist());
            assertThat(updaterStatus.getArtistUrl().equals("") || updaterStatus.getArtistUrl() != null).isFalse();

        }
    }

    @Test
    public void spotifyImageUpdated() throws InstanceNotFoundException {
        Set<ScrobbledArtist> nullUrls = TestResources.dao.getSpotifyNulledUrls();
        SpotifyUpdaterThread spotifyUpdaterThread = new SpotifyUpdaterThread(TestResources.dao);
        spotifyUpdaterThread.run();
        for (ScrobbledArtist nullUrl : nullUrls) {
            UpdaterStatus updaterStatus = TestResources.dao.getUpdaterStatusByName(nullUrl.getArtist());
            assertThat(updaterStatus.getArtistUrl().equals("") || updaterStatus.getArtistUrl() != null).isFalse();

        }
    }
}
