package DAO.Entities;

public class UsersWrapper {
	private long discordID;
	private String lastFMName;
	private int timestamp;

	public UsersWrapper(long discordID, String lastFMName) {
		this.discordID = discordID;
		this.lastFMName = lastFMName;
	}

	UsersWrapper(long discordID, String lastFMName, int timestamp) {
		this.discordID = discordID;
		this.lastFMName = lastFMName;
		this.timestamp = timestamp;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
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
