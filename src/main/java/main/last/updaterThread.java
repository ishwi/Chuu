package main.last;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.UsersWrapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

public class UpdaterThread implements Runnable {
	private Queue<UsersWrapper> queue;
	private DaoImplementation dao;
	private LastFMService last;

	public UpdaterThread(DaoImplementation dao, LastFMService last) {
		this.dao = dao;
		this.last = last;
	}

	@Override
	public void run() {
		UsersWrapper usertoWork = dao.getLessUpdated();
		try {
			LinkedList<ArtistData> artistDataLinkedList = last.getLibrary(usertoWork.getLastFMName());
			dao.addData(artistDataLinkedList, usertoWork.getLastFMName());
			queue.add(usertoWork);
			System.out.println("Updated Info of" + usertoWork + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
		} catch (LastFMServiceException e) {
			e.printStackTrace();
		}

	}
}
