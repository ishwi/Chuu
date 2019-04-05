package main.last;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.UsersWrapper;
import net.dv8tion.jda.api.MessageBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class UpdaterThread implements Runnable {

	private final DaoImplementation dao;
	private UsersWrapper username;

	public UpdaterThread(DaoImplementation dao) {
		this.dao = dao;

	}

	public UpdaterThread(DaoImplementation dao, UsersWrapper username) {
		this.dao = dao;
		this.username = username;

	}

	@Override
	public void run() {
		System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());
		UsersWrapper usertoWork;
		if (this.username == null) {
			usertoWork = dao.getLessUpdated();
		} else
			usertoWork = this.username;

		try {
			LinkedList<ArtistData> artistDataLinkedList = ConcurrentLastFM.getLibrary(usertoWork.getLastFMName());
			dao.addData(artistDataLinkedList, usertoWork.getLastFMName());

			System.out.println("Updated Info of" + usertoWork + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			MessageBuilder a = new MessageBuilder();

		} catch (LastFMServiceException e) {
			e.printStackTrace();
		}

	}
}
