package main.commands;

import dao.DaoImplementation;
import main.apis.last.ConcurrentLastFM;
import main.apis.last.LastFMFactory;

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