package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.ArtistInfo;
import dao.entities.UpdaterStatus;
import main.apis.discogs.DiscogsApi;
import main.apis.spotify.Spotify;
import main.apis.last.ConcurrentLastFM;
import main.exceptions.DiscogsServiceException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CommandUtil {
	static String noImageUrl(String artist) {
		return artist == null || artist
				.isEmpty() ? "https://lastfm-img2.akamaized.net/i/u/174s/4128a6eb29f94943c9d206c08e625904" : artist;
	}

	public static Color randomColor() {
		Random rand = new Random();
		double r = rand.nextFloat() / 2f + 0.5;
		double g = rand.nextFloat() / 2f + 0.5;
		double b = rand.nextFloat() / 2f + 0.5;
		return new Color((float) r, (float) g, (float) b);
	}

//	static CompletableFuture<String> getDiscogsUrlAync(DiscogsApi discogsApi, String artist, DaoImplementation dao) {
//		return CompletableFuture.supplyAsync(() -> updateUrl(discogsApi, artist, dao));
//	}

	public static boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	static BufferedImage getLogo(DaoImplementation dao, MessageReceivedEvent e) {
		try (InputStream stream = dao.findLogo(e.getGuild().getIdLong())) {
			if (stream != null)
				return ImageIO.read(stream);
		} catch (IOException ex) {
			return null;
		}
		return null;
	}

	public static void valiate(DaoImplementation dao, ArtistData artistData, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify, Map<ArtistData, String> adder) {
		String correction = dao.findCorrection(artistData.getArtist());
		boolean updaterBit;
		boolean corrected = false;
		if (correction != null) {
			artistData.setArtist(correction);
			corrected = true;
		}
		UpdaterStatus status = dao.getUpdaterStatus(artistData.getArtist());
		updaterBit = status != null && status.isCorrection_status();
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


			}
			//The artist was checked before so it it were to have a correction it would have hit first if. so it we reach here we can assume that it has no correction

		}

		if (status == null || status.getArtistUrl() == null)
			artistData.setUrl(CommandUtil.updateUrl(discogsApi, artistData.getArtist(), dao, spotify));
		else {
			artistData.setUrl(status.getArtistUrl());
		}
		artistData.setUpdateBit(updaterBit);
	}

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

	static String getArtistImageUrl(DaoImplementation dao, String artist, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify) {
		ArtistData artistData = new ArtistData(artist, 0, "");
		CommandUtil.lessHeavyValidate(dao, artistData, lastFM, discogsApi, spotify);
		return artistData.getUrl();
	}

	static void lessHeavyValidate(DaoImplementation dao, ArtistData artistData, ConcurrentLastFM lastFM, DiscogsApi discogsApi, Spotify spotify) {
		String correction = dao.findCorrection(artistData.getArtist());
		boolean corrected = false;
		boolean needUrlCheck = false;
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
					dao.insertCorrection(artistData.getArtist(), correction);
					artistData.setArtist(correction);

				}


			} else if (status.getCorrection() != null && !status.getCorrection().isEmpty()) {
				artistData.setArtist(status.getCorrection());
				needUrlCheck = true;
			}

		}
		//The artist was checked before so it it were to have a correction it would have hit first if. so it we reach here we can assume that it has no correction
		if (needUrlCheck) {
			artistData.setUrl(dao.getArtistUrl(artistData.getArtist()));
			return;
		}

		if (status == null || status.getArtistUrl() == null)
			artistData.setUrl(CommandUtil.updateUrl(discogsApi, artistData.getArtist(), dao, spotify));
		else {
			artistData.setUrl(status.getArtistUrl());
		}
	}

	static String onlyCorrection(DaoImplementation dao, String artist, ConcurrentLastFM lastFM) {
		String correction = dao.findCorrection(artist);
		if (correction != null) {
			return correction;
		} else {

			UpdaterStatus status = dao.getUpdaterStatus(artist);
			//New artist inexistent in database or never checked before
			if (status == null || !status.isCorrection_status()) {
				correction = lastFM.getCorrection(artist);

				if (artist.equalsIgnoreCase(correction)) {
					artist = correction;
				}


			}
			return artist;
		}
	}

	public static String getLastFmArtistUrl(String artist) {
		return "https://www.last.fm/music/" + artist.replaceAll(" ", "+").replaceAll("[)]", "%29");

	}

	static String getLastFmArtistUserUrl(String artist, String username) {
		return getLastFmUser(username) + "/library/music/" + artist.replaceAll(" ", "+")
				.replaceAll("[)]", "%29");
	}

	public static String getLastFmUser(String username) {
		return "https://www.last.fm/user/" + username;
	}

	public static String singlePlural(int count, String singular, String plural) {
		return count == 1 ? singular : plural;
	}

	static Long getGuildIdConsideringPrivateChannel(MessageReceivedEvent e) {
		if (e.getChannelType().isGuild())
			return (e.getGuild().getIdLong());
		else {
			User user;
			if ((user = e.getJDA().getUserById(e.getAuthor().getIdLong())) == null) {
				return null;
			} else {
				List<Guild> mutualGuilds = user.getMutualGuilds();
				if (mutualGuilds.isEmpty()) {
					return null;
				} else {

					return mutualGuilds.get(0).getIdLong();

				}
			}
		}
	}

}