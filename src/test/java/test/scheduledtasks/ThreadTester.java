package test.scheduledtasks;

import core.exceptions.InstanceNotFoundException;
import core.scheduledtasks.ImageUpdaterThread;
import core.scheduledtasks.SpotifyUpdaterThread;
import core.scheduledtasks.UpdaterThread;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import dao.entities.UpdaterUserWrapper;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import test.commands.utils.TestResources;

import java.util.Collections;
import java.util.Set;

public class ThreadTester {
    @ClassRule
    public static final TestRule res = TestResources.INSTANCE;

    @Test
    public void testIncremental() {

        UpdaterUserWrapper lessUpdated = TestResources.dao.getLessUpdated();
        int timestampControl = lessUpdated.getTimestampControl();
        UpdaterThread updaterThread = new UpdaterThread(TestResources.dao, true);
        updaterThread.run();
        UpdaterUserWrapper lessUpdated2 = TestResources.dao.getLessUpdated();
        if (lessUpdated2.getLastFMName().equals(lessUpdated.getLastFMName())) {
            Assert.assertTrue(lessUpdated2.getTimestampControl() > lessUpdated.getTimestampControl());
        } else {
            updaterThread.run();
            lessUpdated2 = TestResources.dao.getLessUpdated();
            Assert.assertEquals(lessUpdated2.getLastFMName(), lessUpdated.getLastFMName());
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
        Assert.assertEquals(0, TestResources.dao
                .getArtistPlays(-1L, lessUpdated.getLastFMName()));

    }

    @Test
    public void discogsImageUpdated() throws InstanceNotFoundException {
        Set<ScrobbledArtist> nullUrls = TestResources.dao.getNullUrls();
        ImageUpdaterThread imageUpdaterThread = new ImageUpdaterThread(TestResources.dao);
        imageUpdaterThread.run();
        for (ScrobbledArtist nullUrl : nullUrls) {
            UpdaterStatus updaterStatus = TestResources.dao.getUpdaterStatusByName(nullUrl.getArtist());
            Assert.assertTrue(updaterStatus.getArtistUrl() == "" || updaterStatus.getArtistUrl() != null);

        }
    }

    @Test
    public void spotifyImageUpdated() throws InstanceNotFoundException {
        Set<ScrobbledArtist> nullUrls = TestResources.dao.getSpotifyNulledUrls();
        SpotifyUpdaterThread spotifyUpdaterThread = new SpotifyUpdaterThread(TestResources.dao);
        spotifyUpdaterThread.run();
        for (ScrobbledArtist nullUrl : nullUrls) {
            UpdaterStatus updaterStatus = TestResources.dao.getUpdaterStatusByName(nullUrl.getArtist());
            Assert.assertTrue(updaterStatus.getArtistUrl().equals("") || updaterStatus.getArtistUrl() != null);

        }
    }
}
