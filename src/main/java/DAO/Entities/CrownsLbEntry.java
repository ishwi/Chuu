package DAO.Entities;

public class CrownsLbEntry extends LbEntry {

	public CrownsLbEntry(String user, long discordId, int entryCount) {
		super(user, discordId, entryCount);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](https://www.last.fm/user/" +
				getLastFmId() +
				") - " + getEntryCount() +
				" crowns\n";
	}

}
