package core.scheduledtasks;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.CommandUtil;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmEntityNotFoundException;
import dao.DaoImplementation;
import dao.entities.ArtistData;
import dao.entities.TimestampWrapper;
import dao.entities.UpdaterUserWrapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UpdaterThread implements Runnable {

	private final DaoImplementation dao;
	private final ConcurrentLastFM lastFM;
	private final Spotify spotify;
	private final boolean isIncremental;
	private final DiscogsApi discogsApi;


	public UpdaterThread(DaoImplementation dao, boolean isIncremental) {
		this.dao = dao;
		lastFM = LastFMFactory.getNewInstance();
		spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
		discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.isIncremental = isIncremental;
	}

	@Override
	public void run() {
		try {
			System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
			UpdaterUserWrapper userWork;
			Random r = new Random();
			float chance = r.nextFloat();
			userWork = dao.getLessUpdated();

			try {
				if (isIncremental && chance <= 0.93f) {
					Map<ArtistData, String> correctionAdder = new HashMap<>();

					TimestampWrapper<List<ArtistData>> artistDataLinkedList = lastFM
							.getWhole(userWork.getLastFMName(),
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