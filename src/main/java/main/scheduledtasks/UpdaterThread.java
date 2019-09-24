package main.scheduledtasks;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.TimestampWrapper;
import dao.entities.UpdaterUserWrapper;
import main.Chuu;
import main.apis.discogs.DiscogsApi;
import main.apis.last.ConcurrentLastFM;
import main.apis.last.LastFMFactory;
import main.apis.spotify.Spotify;
import main.apis.spotify.SpotifySingleton;
import main.commands.CommandUtil;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFmEntityNotFoundException;

public class UpdaterThread implements Runnable {

	private final DaoImplementation dao;
	private final ConcurrentLastFM lastFM;
	private final Spotify spotify;
	private UpdaterUserWrapper username;
	private boolean isIncremental;
	private DiscogsApi discogsApi;

	private UpdaterThread(DaoImplementation dao) {
		this.dao = dao;
		lastFM = LastFMFactory.getNewInstance();
		spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
	}

	private UpdaterThread(DaoImplementation dao, UpdaterUserWrapper username, boolean isIncremental) {
		this(dao);
		this.username = username;
		this.isIncremental = isIncremental;
	}

	public UpdaterThread(DaoImplementation dao, UpdaterUserWrapper username, boolean isIncremental,
			DiscogsApi discogsApi) {
		this(dao, username, isIncremental);
		this.discogsApi = discogsApi;
	}

	@Override
	public void run() {
		try {
			System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
			UpdaterUserWrapper userWork;
			Random r = new Random();
			float chance = r.nextFloat();

			if (username == null) {
				userWork = dao.getLessUpdated();
			} else {
				userWork = username;
			}

			try {
				if (isIncremental && chance <= 0.93f) {
					Map<ArtistData, String> correctionAdder = new HashMap<>();

					TimestampWrapper<List<ArtistData>> artistDataLinkedList = lastFM.getWhole(userWork.getLastFMName(),
							userWork.getTimestamp());

					// Correction with current last fm implementation should return the same name so
					// no correction gives
					for (ArtistData datum : artistDataLinkedList.getWrapped()) {
						CommandUtil.valiate(dao, datum, lastFM, discogsApi, spotify, correctionAdder);
					}

					dao.incrementalUpdate(artistDataLinkedList, userWork.getLastFMName());

					// Workarround non deferrable foreign key
					correctionAdder.forEach((correctedArtistData, originalArtistName) -> dao
							.insertCorrection(originalArtistName, correctedArtistData.getArtist()));

					System.out.println("Updated Info Incrementally of " + userWork.getLastFMName()
							+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
					System.out.println(" Number of rows updated :" + artistDataLinkedList.getWrapped().size());
				} else {

					List<ArtistData> artistDataLinkedList = lastFM.getLibrary(userWork.getLastFMName());
					dao.insertArtistDataList(artistDataLinkedList, userWork.getLastFMName());
					System.out.println("Updated Info Normally  of " + userWork.getLastFMName()
							+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
					System.out.println(" Number of rows updated :" + artistDataLinkedList.size());
				}
			} catch (LastFMNoPlaysException e) {
				// dao.updateUserControlTimestamp(userWork.getLastFMName(),userWork.getTimestampControl());
				dao.updateUserTimeStamp(userWork.getLastFMName(), userWork.getTimestamp(),
						(int) (Instant.now().getEpochSecond() + 4000));
				System.out.println("No plays " + userWork.getLastFMName()
						+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			} catch (LastFmEntityNotFoundException e) {
				dao.removeUserCompletely(userWork.getDiscordID());
			}

		} catch (Throwable e) {
			System.out.println("Error while updating" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			Chuu.getLogger().warn(e.getMessage(), e);
		}
	}
}