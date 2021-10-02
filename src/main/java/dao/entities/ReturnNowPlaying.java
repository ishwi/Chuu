package dao.entities;

import main.commands.CommandUtil;

import java.awt.*;

public class ReturnNowPlaying {
	private final String artist;
	private long discordId;
	private String discordName;
	private Color roleColor;
	private String lastFMId;
	private int playNumber;

	public ReturnNowPlaying(long discordId, String lastFMId, String artist, int playNumber) {
		this.discordId = discordId;
		this.lastFMId = lastFMId;
		this.artist = artist;
		this.playNumber = playNumber;
	}

	public long getDiscordId() {
		return discordId;
	}

	public void setDiscordId(long discordId) {
		this.discordId = discordId;
	}

	public String getDiscordName() {
		return discordName;
	}

	public void setDiscordName(String discordName) {
		this.discordName = discordName;
	}

	public Color getRoleColor() {
		return roleColor;
	}

	public void setRoleColor(Color roleColor) {
		this.roleColor = roleColor;
	}

	public String getArtist() {
		return artist;
	}

	public String getLastFMId() {
		return lastFMId;
	}

	public void setLastFMId(String lastFMId) {
		this.lastFMId = lastFMId;
	}


	public int getPlayNumber() {
		return playNumber;
	}

	public void setPlayNumber(int playNumber) {
		this.playNumber = playNumber;
	}

	@Override
	public String toString() {
		return ". " +
				"[" + discordName + "](" +
				CommandUtil
						.getLastFmArtistUserUrl(artist, lastFMId) +
				") - " +
				playNumber + " plays\n";
	}
}