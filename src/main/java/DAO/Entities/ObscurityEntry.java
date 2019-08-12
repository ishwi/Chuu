package DAO.Entities;

public class ObscurityEntry extends LbEntry {
	public ObscurityEntry(String lastFMId, long discordId, int crowns) {
		super(lastFMId, discordId, crowns);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](https://www.last.fm/user/" +
				getLastFmId() +
				") - " + getEntryCount() +
				" obscurity points\n";
	}

}

