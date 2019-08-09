package DAO.Entities;

public class UniqueLbEntry extends LbEntry {

	public UniqueLbEntry(String user, long discordId, int entryCount) {
		super(user, discordId, entryCount);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](https://www.last.fm/user/" +
				getLastFmId() +
				") - " + getEntryCount() +
				" unique artists\n";
	}

}
