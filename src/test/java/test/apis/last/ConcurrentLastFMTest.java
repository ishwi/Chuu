package test.apis.last;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import dao.entities.ArtistAlbums;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.Track;
import core.exceptions.LastFMServiceException;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConcurrentLastFMTest {

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
			List<Track> secondsTimeFrameCount = lastFM.getTopArtistTracks("lukyfan", "Northlane", "7day");

		} catch (LastFmException ignored) {
		}
	}
}