package main.Commands;

import DAO.DaoImplementation;
import main.APIs.Spotify.Spotify;

abstract class MyCommandDbAndSpotifyAccess extends MyCommandDbAccess {
	private final Spotify spotify;

	public MyCommandDbAndSpotifyAccess(DaoImplementation dao, Spotify spotify) {
		super(dao);
		this.spotify = spotify;
	}
}
