package DAO.Entities;

public class UpdaterWrapper<T> {
	private int timestamp;
	private String username;
	private T nowPlayingArtistList;

	public UpdaterWrapper(int timestamp, String username, T nowPlayingArtistList) {
		this.timestamp = timestamp;
		this.username = username;
		this.nowPlayingArtistList = nowPlayingArtistList;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public String getUsername() {
		return username;
	}

	public T getNowPlayingArtistList() {
		return nowPlayingArtistList;
	}
}
