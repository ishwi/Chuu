package main.Commands;

import DAO.DaoImplementation;
import main.APIs.Spotify.Spotify;

public abstract class MyCommandDbAndSpotifyAccess extends MyCommandDbAccess {
	public Spotify spotify;

	public MyCommandDbAndSpotifyAccess(DaoImplementation dao, Spotify spotify) {
		super(dao);
		this.spotify = spotify;
	}
}
