package main.APIs.last;

import DAO.Entities.ArtistAlbums;
import main.Exceptions.LastFMServiceException;
import main.Exceptions.LastFmException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConcurrentLastFMTest {

	@Test
	public void getAlbumsFromArtist() throws LastFmException {
		ConcurrentLastFM lastFM = new ConcurrentLastFM();

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
}