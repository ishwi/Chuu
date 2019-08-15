package DAO.Entities;

import main.Commands.CommandUtil;

public class UniqueData {
	private final int count;
	private final String artistName;

	public UniqueData(String name, int count_a) {
		this.artistName = name;
		this.count = count_a;
	}

	@Override
	public String toString() {
		String a = ". [" +
				getArtistName() +
				"](" + CommandUtil.getLastFmArtistUrl(artistName) +
				") - " + getCount() +
				" plays\n";
		return a;
	}

	public String getArtistName() {
		return artistName;
	}

	private int getCount() {
		return count;
	}
}
