package dao.entities;

import core.commands.CommandUtil;

public class StolenCrown {
	private final String artist;
	private final int ogPlays;
	private final int queriedPlays;

	public StolenCrown(String artist, int ogPlays, int queriedPlays) {
		this.artist = artist;
		this.ogPlays = ogPlays;
		this.queriedPlays = queriedPlays;
	}

	public String getArtist() {
		return artist;
	}

	public int getOgPlays() {
		return ogPlays;
	}

	public int getQueriedPlays() {
		return queriedPlays;
	}

	@Override
	public String toString() {
		return ". [" +
				artist +
				"](" + CommandUtil.getLastFmArtistUrl(artist) +
				") : " + ogPlays +
				" -> " + queriedPlays + "\n";
	}
}
