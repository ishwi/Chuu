package DAO.Entities;

class UpdaterWrapper<T> {
	private final int timestamp;
	private final String username;
	private final T nowPlayingArtistList;

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
