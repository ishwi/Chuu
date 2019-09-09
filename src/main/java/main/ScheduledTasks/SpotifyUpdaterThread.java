package main.ScheduledTasks;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import main.APIs.Spotify.Spotify;

import java.util.Set;

public class SpotifyUpdaterThread implements Runnable {
	private final DaoImplementation dao;
	private final Spotify spotifyApi;

	public SpotifyUpdaterThread(DaoImplementation dao, Spotify api) {
		this.dao = dao;
		this.spotifyApi = api;

	}

	@Override
	public void run() {
		Set<String> artistData = dao.getSpotifyNulledUrls();
		System.out.println("Found at lest Spotify " + artistData.size() + "null artist ");
		for (String artistDatum : artistData) {
			String url;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Working with artist " + artistDatum);
			url = spotifyApi.getArtistUrlImage(artistDatum);
			if (url != null) {
				if (!url.isEmpty())
					System.out.println("INSERTED : " + artistDatum);
				dao.upsertSpotify(new ArtistInfo(url, artistDatum));
			}

		}


	}


}
