package DAO.Entities;

public class ReturnNowPlaying {
	private long discordId;
	private String lastFMId;
	private final String artist;
	private int playnumber;

	public ReturnNowPlaying(long discordId, String lastFMId, String artist, int playnumber) {
		this.discordId = discordId;
		this.lastFMId = lastFMId;
		this.artist = artist;
		this.playnumber = playnumber;
	}

	public long getDiscordId() {
		return discordId;
	}

	public void setDiscordId(long discordId) {
		this.discordId = discordId;
	}

	public String getLastFMId() {
		return lastFMId;
	}

	public void setLastFMId(String lastFMId) {
		this.lastFMId = lastFMId;
	}


	public int getPlaynumber() {
		return playnumber;
	}

	public void setPlaynumber(int playnumber) {
		this.playnumber = playnumber;
	}

}