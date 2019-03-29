package DAO;

public class ArtistData {
	private String discordID;
	private String artist;
	private int count;

	public ArtistData(String discordID, String artist, int count) {
		this.discordID = discordID;
		this.artist = artist;
		this.count = count;
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
}
