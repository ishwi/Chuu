package main.APIs.last;

import DAO.Entities.ArtistAlbums;
import DAO.Entities.SecondsTimeFrameCount;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.LastFmException;
import org.junit.Test;

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

	@Test(expected = LastFMNoPlaysException.class)
	public void EntityNotFound() throws LastFmException {
		ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

		try {
			SecondsTimeFrameCount secondsTimeFrameCount = lastFM.getMinutesWastedOnMusicWeek("a8zm219xm1-09cu-");
			int seconds = secondsTimeFrameCount.getSeconds();
			int count = secondsTimeFrameCount.getCount();

		} catch (LastFMServiceException ignored) {
		}


	}

	@Test
	public void getSeconds() throws LastFmException {
		ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

		try {
			SecondsTimeFrameCount secondsTimeFrameCount = lastFM.getMinutesWastedOnMusicWeek("lukyfan");
			int seconds = secondsTimeFrameCount.getSeconds();
			int count = secondsTimeFrameCount.getCount();

		} catch (LastFMServiceException ignored) {
		}


	}
}