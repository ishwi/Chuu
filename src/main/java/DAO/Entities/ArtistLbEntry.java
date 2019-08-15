package DAO.Entities;

import main.Commands.CommandUtil;

public class ArtistLbEntry extends LbEntry {
	public ArtistLbEntry(String lastFMId, long discordId, int artistCount) {
		super(lastFMId, discordId, artistCount);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
				") - " + getEntryCount() +
				" artists\n";
	}

}

