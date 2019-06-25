package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.ArtistInfo;
import DAO.Entities.UpdaterStatus;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Spotify.Spotify;
import main.APIs.last.ConcurrentLastFM;
import main.Exceptions.DiscogsServiceException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Random;

public class CommandUtil {
	public static String updateUrl(DiscogsApi discogsApi, String artist, DaoImplementation dao, Spotify spotify) {
		String newUrl = null;
		try {
			newUrl = discogsApi.findArtistImage(artist);
			if (!newUrl.isEmpty()) {
				dao.upsertUrl(new ArtistInfo(newUrl, artist));
			} else {
				newUrl = spotify.getArtistUrlImage(artist);
				dao.upsertSpotify(new ArtistInfo(newUrl, artist));
			}
		} catch (DiscogsServiceException ignored) {

		}
		return newUrl;
	}

	static String noImageUrl(String artist) {
		return artist == null || artist.isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
	}

//	static CompletableFuture<String> getDiscogsUrlAync(DiscogsApi discogsApi, String artist, DaoImplementation dao) {
//		return CompletableFuture.supplyAsync(() -> updateUrl(discogsApi, artist, dao));
//	}


	public static Color randomColor() {
		Random rand = new Random();
		double r = rand.nextFloat() / 2f + 0.5;
		double g = rand.nextFloat() / 2f + 0.5;
		double b = rand.nextFloat() / 2f + 0.5;
		return new Color((float) r, (float) g, (float) b);
	}

	public static boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	public static BufferedImage getLogo(DaoImplementation dao, MessageReceivedEvent e) {
		try (InputStream stream = dao.findLogo(e.getGuild().getIdLong())) {
			if (stream != null)
				return ImageIO.read(stream);
		} catch (IOException ex) {
			return null;
		}
		return null;
	}

	public static void validatesArtistInfo(DaoImplementation dao, DiscogsApi discogsApi, Spotify spotify, ArtistData datum, ConcurrentLastFM lastFM) {


		//jambi -> jambi2
		UpdaterStatus status = dao.getUpdaterStatus(datum.getArtist());
		if (status == null) {
			String correction = lastFM.getCorrection(datum.getArtist());
			dao.createCorrection(datum.getArtist(), correction);


			datum.setArtist(correction);

			//Tempo fix could be insert precorrected artist to artist_url
			return;
		}

		if (status.getArtistUrl() == null)
			status.setArtistUrl(CommandUtil.updateUrl(discogsApi, datum.getArtist(), dao, spotify));

		//Never checked if it needs correction
		if (!status.isCorrection_status()) {
			String correction = lastFM.getCorrection(datum.getArtist());
			dao.createCorrection(datum.getArtist(), correction);
			datum.setArtist(correction);
		} else if (status.getCorrection() != null) {
			datum.setArtist(status.getCorrection());
		}
		datum.setUrl(status.getArtistUrl());
	}

	public static void valiate(DaoImplementation dao, ArtistData artistData, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify, Map<ArtistData, String> adder) {
		String correction = dao.findCorrection(artistData.getArtist());
		boolean updaterBit = false;
		boolean corrected = false;
		boolean addToQueue = false;
		if (correction != null) {
			artistData.setArtist(correction);
			corrected = true;
		}
		UpdaterStatus status = dao.getUpdaterStatus(artistData.getArtist());

		if (!corrected) {
			//New artist inexistent in database or never checked before
			if (status == null || !status.isCorrection_status()) {
				correction = lastFM.getCorrection(artistData.getArtist());

				//If its different we insert the new correction in the table
				if (!artistData.getArtist().equalsIgnoreCase(correction)) {
					adder.put(artistData, artistData.getArtist());
					//dao.insertCorrection(artistData.getArtist(), correction);
					artistData.setArtist(correction);
				}
				updaterBit = true;
				//Should set a flag instead of doing this
				//Mark the artist as corrected
				//updaterDao.updateStatusBit(connection, correction);

				//The artist was checked before so it it were to have a correction it would have hit first if. so it we reach here we can assume that it has no correction
			}

		}


		if (status == null || status.getArtistUrl() == null)
			artistData.setUrl(CommandUtil.updateUrl(discogsApi, artistData.getArtist(), dao, spotify));
		else {
			artistData.setUrl(status.getArtistUrl());
		}
		artistData.setUpdateBit(updaterBit);
	}
}