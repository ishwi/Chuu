package DAO.Entities;

public class UrlCapsule {


	private final String artistName;
	private final int pos;
	private String url;
	private final String albumName;
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

	public void setUrl(String url) {
		this.url = url;
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
