package dao.entities;

public class EntityInfo {
	private String mbid;
	private String artist;


	EntityInfo(String mbid, String artist) {
		this.mbid = mbid;
		this.artist = artist;
	}

	public EntityInfo(String mbid) {
		this.mbid = mbid;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}


	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
}
