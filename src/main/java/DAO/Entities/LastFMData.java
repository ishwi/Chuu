package DAO.Entities;

public class LastFMData {

	private Long showID;
	private String name;
	private long guildID;

	public LastFMData(String name, Long showID, long guildID) {
		this.showID = showID;
		this.name = name;
		this.guildID = guildID;
	}

	public LastFMData(String lastFmID, long resDiscordID) {
		this.name = lastFmID;
		this.showID = resDiscordID;
	}


	public Long getShowID() {
		return showID;
	}


	public void setShowID(Long showID) {
		this.showID = showID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getGuildID() {
		return guildID;
	}

	public void setGuildID(long guildID) {
		this.guildID = guildID;
	}
}