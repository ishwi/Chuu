package DAO.Entities;

public class AlbumInfo {
	private String mbid;
	private String name;
	private String artist;

	public AlbumInfo(String name, String artist) {
		this.name = name;
		this.artist = artist;
	}

	public AlbumInfo(String mbid, String name, String artist) {
		this.mbid = mbid;
		this.name = name;
		this.artist = artist;
	}

	public AlbumInfo(String mbid) {
		this.mbid = mbid;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
}
