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
		ConcurrentLastFM lastfm = new ConcurrentLastFM();

		try {
			ArtistAlbums artistAlbums = lastfm.getAlbumsFromArtist("cher", 10);
			assertTrue("cher".equalsIgnoreCase(artistAlbums.getArtist()));
			assertEquals(10, artistAlbums.getAlbumList().size());

			artistAlbums = lastfm.getAlbumsFromArtist("cherr", 10);
			assertTrue("cher".equalsIgnoreCase(artistAlbums.getArtist()));
			assertEquals(10, artistAlbums.getAlbumList().size());


		} catch (LastFMServiceException ex) {
			return;
		}


	}
}