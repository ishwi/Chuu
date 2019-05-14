package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistInfo;
import main.Exceptions.DiscogsServiceException;
import main.Youtube.DiscogsApi;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class CommandUtil {
	static String getDiscogsUrl(DiscogsApi discogsApi, String artist, DaoImplementation dao) {
		String newUrl = null;
		try {
			newUrl = discogsApi.findArtistImage(artist);
			if (newUrl != null) {
				dao.upsertUrl(new ArtistInfo(newUrl, artist));
			}
		} catch (DiscogsServiceException ignored) {
		}
		return newUrl;
	}

	static String noImageUrl(String artist) {
		return artist == null || artist.isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
	}

	static CompletableFuture<String> getDiscogsUrlAync(DiscogsApi discogsApi, String artist, DaoImplementation dao) {
		return CompletableFuture.supplyAsync(() -> getDiscogsUrl(discogsApi, artist, dao));
	}


	public static Color randomColor() {
		Random rand = new Random();
		double r = rand.nextFloat() / 2f + 0.5;
		double g = rand.nextFloat() / 2f + 0.5;
		double b = rand.nextFloat() / 2f + 0.5;
		return new Color((float) r, (float) g, (float) b);
	}
}