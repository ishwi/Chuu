package core.scheduledtasks;

import dao.DaoImplementation;
import dao.entities.ArtistInfo;
import core.apis.spotify.Spotify;

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
