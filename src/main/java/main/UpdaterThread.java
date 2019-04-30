package main;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.UsersWrapper;
import main.Exceptions.LastFMNoPlaysException;
import main.Exceptions.LastFMServiceException;
import main.last.ConcurrentLastFM;
import main.last.TimestampWrapper;
import net.dv8tion.jda.core.MessageBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Random;

public class UpdaterThread implements Runnable {

	private final DaoImplementation dao;
	private UsersWrapper username;
	private boolean isIncremental;

	public UpdaterThread(DaoImplementation dao) {
		this.dao = dao;

	}

	public UpdaterThread(DaoImplementation dao, UsersWrapper username, boolean isIncremental) {
		this.dao = dao;
		this.username = username;
		this.isIncremental = isIncremental;

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
				TimestampWrapper<LinkedList<ArtistData>> artistDataLinkedList = ConcurrentLastFM.getWhole(usertoWork.getLastFMName(), usertoWork.getTimestamp());
				dao.incrementalUpdate(artistDataLinkedList, usertoWork.getLastFMName());

				System.out.println("Updated Info Incremetally of " + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
				System.out.println(" Number of rows updated :" + artistDataLinkedList.getWrapped().size());
				MessageBuilder a = new MessageBuilder();
			} else {

				LinkedList<ArtistData> artistDataLinkedList = ConcurrentLastFM.getLibrary(usertoWork.getLastFMName());
				dao.updateUserLibrary(artistDataLinkedList, usertoWork.getLastFMName());

				System.out.println("Updated Info Normally  of " + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
				System.out.println(" Number of rows updated :" + artistDataLinkedList.size());
				MessageBuilder a = new MessageBuilder();
			}
		} catch (LastFMServiceException e) {
			System.out.println("Error while updating" + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));

			e.printStackTrace();
		} catch (LastFMNoPlaysException e) {
			dao.updateUserTimeStamp(usertoWork.getLastFMName());
			System.out.println("No plays " + usertoWork.getLastFMName() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));

		}


	}
}
