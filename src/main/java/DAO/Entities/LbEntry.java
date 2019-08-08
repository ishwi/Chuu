package DAO.Entities;

public abstract class LbEntry {
	private final String lastFmId;
	private final long discordId;
	private final int entryCount;
	private String discordName;

	LbEntry(String user, long discordId, int entryCount) {
		this.lastFmId = user;
		this.discordId = discordId;
		this.entryCount = entryCount;
	}

	String getDiscordName() {
		return discordName;
	}

	public void setDiscordName(String discordName) {
		this.discordName = discordName;
	}

	public long getDiscordId() {
		return discordId;
	}

	int getEntryCount() {
		return entryCount;
	}

	public String getLastFmId() {
		return lastFmId;
	}

	@Override
	public abstract String toString();

}

