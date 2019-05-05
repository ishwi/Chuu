package main;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.UsersWrapper;
import main.Exceptions.DiscogsServiceException;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFmException;
import main.Youtube.DiscogsApi;
import main.last.ConcurrentLastFM;
import main.last.TimestampWrapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class UpdaterThread implements Runnable {

	private final DaoImplementation dao;
	private UsersWrapper username;
	private boolean isIncremental;
	private DiscogsApi discogsApi;
	private ConcurrentLastFM lastFM;

	public UpdaterThread(DaoImplementation dao) {
		this.dao = dao;
		this.lastFM = new ConcurrentLastFM();
	}

	public UpdaterThread(DaoImplementation dao, UsersWrapper username, boolean isIncremental) {
		this(dao);
		this.username = username;
		this.isIncremental = isIncremental;
	}


	public UpdaterThread(DaoImplementation dao, UsersWrapper username, boolean isIncremental, DiscogsApi discogsApi) {
		this(dao, username, isIncremental);
		this.discogsApi = discogsApi;
	}


	@Override
	public void run() {
		System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
		UsersWrapper usertoWork;
		Random r = new Random();
		float chance = r.nextFloat();

		if (this.username == null) {
			usertoWork = dao.getLessUpdated();
		} else
			usertoWork = this.username;

		try {
			if (isIncremental && chance <= 0.90f) {

				TimestampWrapper<LinkedList<ArtistData>> artistDataLinkedList = lastFM.getWhole(usertoWork.getLastFMName(), usertoWork.getTimestamp());
				if (discogsApi != null) {
					CompletableFuture.runAsync(() -> {

						for (ArtistData datum : artistDataLinkedList.getWrapped()) {
							if (dao.getArtistUrl(datum.getArtist()) == null) {
								try {
									String newUrl = discogsApi.findArtistImage(datum.getArtist());
									if (newUrl != null) {
										System.out.println("Upserting buddy");
										datum.setUrl(newUrl);
									}
								} catch (DiscogsServiceException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}

				dao.incrementalUpdate(artistDataLinkedList, usertoWork.getLastFMName());


				System.out.println("Updated Info Incremetally of " + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
				System.out.println(" Number of rows updated :" + artistDataLinkedList.getWrapped().size());


			} else {

				LinkedList<ArtistData> artistDataLinkedList = lastFM.getLibrary(usertoWork.getLastFMName());
				dao.updateUserLibrary(artistDataLinkedList, usertoWork.getLastFMName());

				System.out.println("Updated Info Normally  of " + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
				System.out.println(" Number of rows updated :" + artistDataLinkedList.size());
			}
		} catch (LastFMNoPlaysException e) {
			dao.updateUserTimeStamp(usertoWork.getLastFMName());
			System.out.println("No plays " + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));

		} catch (LastFmException e) {
			System.out.println("Error while updating" + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));

			e.printStackTrace();


		}
	}
}
