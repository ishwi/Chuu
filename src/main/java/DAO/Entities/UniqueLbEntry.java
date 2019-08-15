package DAO.Entities;

import main.Commands.CommandUtil;

public class UniqueLbEntry extends LbEntry {

	public UniqueLbEntry(String user, long discordId, int entryCount) {
		super(user, discordId, entryCount);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
				") - " + getEntryCount() +
				" unique artists\n";
	}

}
