package DAO.Entities;

public class AlbumInfo {
	private final String album;
	private final String album_url;
	private int plays;

	public AlbumInfo(String album, String album_url) {
		this.album = album;
		this.album_url = album_url;
	}

	public String getAlbum() {
		return album;
	}

	public String getAlbum_url() {
		return album_url;
	}

	public int getPlays() {
		return plays;
	}

	public void setPlays(int plays) {
		this.plays = plays;
	}
}
