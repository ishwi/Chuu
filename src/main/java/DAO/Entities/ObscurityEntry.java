package DAO.Entities;

import main.Commands.CommandUtil;

public class ObscurityEntry extends LbEntry {
	public ObscurityEntry(String lastFMId, long discordId, int crowns) {
		super(lastFMId, discordId, crowns);
	}

	@Override
	public String toString() {
		return ". [" +
				getDiscordName() +
				"](" + CommandUtil.getLastFmUser(this.getLastFmId()) +
				"): " + getEntryCount() +
				" obscurity points\n";
	}

}

