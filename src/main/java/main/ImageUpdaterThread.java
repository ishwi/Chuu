package main;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import main.Exceptions.DiscogsServiceException;
import main.Youtube.DiscogsApi;

import java.util.Set;

public class ImageUpdaterThread implements Runnable {
	private final DaoImplementation dao;
	private final DiscogsApi discogsApi;

	public ImageUpdaterThread(DaoImplementation dao) {
		this.dao = dao;
		this.discogsApi = new DiscogsApi();

	}

	@Override
	public void run() {
		Set<String> artistData = dao.getNullUrls();
		try {
			for (String artistDatum : artistData) {

				String url = discogsApi.findArtistImage(artistDatum);
				dao.upsertUrl(new ArtistInfo(url, artistDatum));
			}
		} catch (DiscogsServiceException e) {
			e.printStackTrace();
		}


	}
}
