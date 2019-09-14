package dao.entities;

public class LastFMData {

	private Long discordId;
	private String name;
	private long guildID;

	public LastFMData(String name, Long discordId, long guildID) {
		this.discordId = discordId;
		this.name = name;
		this.guildID = guildID;
	}

	public LastFMData(String lastFmID, long resDiscordID) {
		this.name = lastFmID;
		this.discordId = resDiscordID;
	}


	public Long getDiscordId() {
		return discordId;
	}


	public void setDiscordId(Long discordId) {
		this.discordId = discordId;
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