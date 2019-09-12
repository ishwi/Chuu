package DAO.Entities;

public class AlbumUserPlays {
	private String album;
	private String album_url;
	private int plays;


	public AlbumUserPlays(String album, String album_url) {
		this.album = album;
		this.album_url = album_url;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAlbum_url() {
		return album_url;
	}

	public void setAlbum_url(String album_url) {
		this.album_url = album_url;
	}

	public int getPlays() {
		return plays;
	}

	public void setPlays(int plays) {
		this.plays = plays;
	}
}
