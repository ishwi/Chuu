package DAO.Entities;

public class UsersWrapper {
	private long discordID;
	private String lastFMName;

	public UsersWrapper(long discordID, String lastFMName) {
		this.discordID = discordID;
		this.lastFMName = lastFMName;
	}

	public long getDiscordID() {
		return discordID;
	}

	public void setDiscordID(long discordID) {
		this.discordID = discordID;
	}

	public String getLastFMName() {
		return lastFMName;
	}

	public void setLastFMName(String lastFMName) {
		this.lastFMName = lastFMName;
	}
}
