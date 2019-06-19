package main.Commands;

import DAO.DaoImplementation;
import main.APIs.last.ConcurrentLastFM;

abstract class MyCommandDbAccess extends MyCommand {
	final ConcurrentLastFM lastFM;
	private final DaoImplementation dao;

	MyCommandDbAccess(DaoImplementation dao) {
		this.dao = dao;
		lastFM = new ConcurrentLastFM();
	}

	DaoImplementation getDao() {
		return dao;
	}
}