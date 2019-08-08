package main.ScheduledTasks;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.TimestampWrapper;
import DAO.Entities.UpdaterUserWrapper;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.APIs.last.ConcurrentLastFM;
import main.APIs.last.LastFMFactory;
import main.Chuu;
import main.Commands.CommandUtil;
import main.Exceptions.LastFMNoPlaysException;

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
	private UpdaterUserWrapper username;
	private boolean isIncremental;
	private DiscogsApi discogsApi;

	public UpdaterThread(DaoImplementation dao, UpdaterUserWrapper username, boolean isIncremental, DiscogsApi discogsApi) {
		this(dao, username, isIncremental);
		this.discogsApi = discogsApi;
	}

	public UpdaterThread(DaoImplementation dao, UpdaterUserWrapper username, boolean isIncremental) {
		this(dao);
		this.username = username;
		this.isIncremental = isIncremental;
	}


	private UpdaterThread(DaoImplementation dao) {
		this.dao = dao;
		this.lastFM = LastFMFactory.getNewInstance();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
	}

	@Override
	public void run() {
		System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
		UpdaterUserWrapper userWork;
		Random r = new Random();
		float chance = r.nextFloat();

		if (this.username == null) {
			userWork = dao.getLessUpdated();
		} else
			userWork = this.username;

		try {
			if (isIncremental && chance <= 0.93f) {
				Map<ArtistData, String> correctionAdder = new HashMap<>();

				TimestampWrapper<List<ArtistData>> artistDataLinkedList = lastFM
						.getWhole(userWork.getLastFMName(), userWork.getTimestamp());

				//Correction with current last fm implementation should return the same name so no correction gives
				for (ArtistData datum : artistDataLinkedList.getWrapped()) {
					CommandUtil.valiate(dao, datum, lastFM, discogsApi, spotify, correctionAdder);
				}

				dao.incrementalUpdate(artistDataLinkedList, userWork.getLastFMName());

				//Workarround non deferrable foreign key
				correctionAdder.forEach((correctedArtistData, originalArtistName) ->
						dao.insertCorrection(originalArtistName, correctedArtistData.getArtist()));

				System.out.println("Updated Info Incrementally of " + userWork.getLastFMName() + LocalDateTime.now()
						.format(DateTimeFormatter.ISO_DATE));
				System.out.println(" Number of rows updated :" + artistDataLinkedList.getWrapped().size());
			} else {

				List<ArtistData> artistDataLinkedList = lastFM.getLibrary(userWork.getLastFMName());
				dao.insertArtistDataList(artistDataLinkedList, userWork.getLastFMName());
				if (!artistDataLinkedList.isEmpty()) {
					Chuu.updatePresence(artistDataLinkedList.get(new Random().nextInt(artistDataLinkedList.size()))
							.getArtist());

				}

				System.out.println("Updated Info Normally  of " + userWork.getLastFMName() + LocalDateTime.now()
						.format(DateTimeFormatter.ISO_DATE));
				System.out.println(" Number of rows updated :" + artistDataLinkedList.size());
			}
		} catch (LastFMNoPlaysException e) {
			//dao.updateUserControlTimestamp(userWork.getLastFMName(),userWork.getTimestampControl());
			dao.updateUserTimeStamp(userWork.getLastFMName(), userWork.getTimestamp(), (int) (Instant.now()
					.getEpochSecond() + 600));
			System.out.println("No plays " + userWork.getLastFMName() + LocalDateTime.now()
					.format(DateTimeFormatter.ISO_DATE));

		} catch (Throwable e) {
			System.out.println("Error while updating" + userWork.getLastFMName() + LocalDateTime.now()
					.format(DateTimeFormatter.ISO_DATE));
			e.printStackTrace();
		}
	}
}