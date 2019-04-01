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
	private final LastFMService last;

	public UpdaterThread(DaoImplementation dao, LastFMService last) {
		this.dao = dao;
		this.last = last;
	}

	@Override
	public void run() {
		System.out.println("THREAD WORKING ) + " + LocalDateTime.now().toString());

		UsersWrapper usertoWork = dao.getLessUpdated();
		try {
			LinkedList<ArtistData> artistDataLinkedList = last.getLibrary(usertoWork.getLastFMName());
			dao.addData(artistDataLinkedList, usertoWork.getLastFMName());

			System.out.println("Updated Info of" + usertoWork + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			MessageBuilder a = new MessageBuilder();

		} catch (LastFMServiceException e) {
			e.printStackTrace();
		}

	}
}
