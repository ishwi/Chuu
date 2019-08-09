package DAO.Entities;

public class ArtistLbEntry extends LbEntry {
	public ArtistLbEntry(String lastFMId, long discordId, int artistCount) {
		super(lastFMId, discordId, artistCount);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](https://www.last.fm/user/" +
				getLastFmId() +
				") - " + getEntryCount() +
				" artists\n";
	}

}

