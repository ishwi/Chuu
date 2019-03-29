package DAO;

public class ArtistData {
	private String discordID;
	private String artist;
	private int count;
	private String url;

	public ArtistData(String discordID, String artist, int count) {
		this.discordID = discordID;
		this.artist = artist;
		this.count = count;
	}

	public ArtistData(String artist, int count, String url) {
		this.artist = artist;
		this.count = count;
		this.url = url;
	}

	public String getDiscordID() {
		return discordID;
	}

	public void setDiscordID(String discordID) {
		this.discordID = discordID;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {

		this.url = url;
	}
}
