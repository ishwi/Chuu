package dao.entities;

import core.commands.CommandUtil;

public class UniqueData {
	private final int count;
	private final String artistName;

	public UniqueData(String name, int count_a) {
		this.artistName = name;
		this.count = count_a;
	}

	@Override
	public String toString() {
		return ". [" +
				getArtistName() +
				"](" + CommandUtil.getLastFmArtistUrl(artistName) +
				") - " + getCount() +
				" plays\n";
	}

	public String getArtistName() {
		return artistName;
	}

	private int getCount() {
		return count;
	}
}
