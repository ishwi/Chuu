package DAO.Entities;

public class UrlCapsule {


	private final String url;
	private final int pos;
	private final String albumName;
	private final String artistName;
	private int plays;

	public UrlCapsule(String url, int pos, String albumName, String artistName) {
		this.url = url;
		this.pos = pos;
		this.artistName = artistName;
		this.albumName = albumName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public String getArtistName() {
		return artistName;
	}

	public int getPlays() {
		return plays;
	}

	public void setPlays(int plays) {
		this.plays = plays;
	}

	public int getPos() {
		return pos;
	}

	public String getUrl() {
		return url;
	}
}
