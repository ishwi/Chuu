package DAO.Entities;

public class UrlCapsule {


	private final String artistName;
	private final String albumName;
	private int pos;
	private String url;
	private int plays;
	private String mbid;

	public UrlCapsule(String url, int pos, String albumName, String artistName, String mbid) {
		this.url = url;
		this.pos = pos;
		this.artistName = artistName;
		this.albumName = albumName;
		this.mbid = mbid;
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

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
}
