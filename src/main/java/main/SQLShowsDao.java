package main;

import java.sql.Connection;

public interface SQLShowsDao {
	public LastFMData create(Connection con, LastFMData show);

	public LastFMData find(Connection con, Long showID) ;

	public void update(Connection con, LastFMData show);

	public void remove(Connection con, Long showID) ;

}
