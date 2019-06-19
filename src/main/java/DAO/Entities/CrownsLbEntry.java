package DAO.Entities;

public class CrownsLbEntry {
	private final String lastFmId;
	private final long discordId;
	private final int crowns;
	private String discordName;

	public CrownsLbEntry(String user, long discordId, int crowns) {
		this.lastFmId = user;
		this.discordId = discordId;
		this.crowns = crowns;
	}

	public String getDiscordName() {
		return discordName;
	}

	public void setDiscordName(String discordName) {
		this.discordName = discordName;
	}

	public long getDiscordId() {
		return discordId;
	}

	public int getCrowns() {
		return crowns;
	}

	public String getLastFmId() {
		return lastFmId;
	}


	public String toString() {
		String strID = discordName;
		String a = ". [" +
				strID +
				"](https://www.last.fm/user/" +
				lastFmId +
				") - " + crowns +
				" crowns\n";
		return a;
	}

}
