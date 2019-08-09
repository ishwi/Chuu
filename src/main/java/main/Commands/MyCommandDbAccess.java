package main.Commands;

import DAO.DaoImplementation;
import main.APIs.last.ConcurrentLastFM;
import main.APIs.last.LastFMFactory;

abstract class MyCommandDbAccess extends MyCommand {
	final ConcurrentLastFM lastFM;
	private final DaoImplementation dao;

	MyCommandDbAccess(DaoImplementation dao) {
		this.dao = dao;
		lastFM = LastFMFactory.getNewInstance();
	}

	DaoImplementation getDao() {
		return dao;
	}
}