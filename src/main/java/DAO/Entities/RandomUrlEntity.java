package DAO.Entities;

public class RandomUrlEntity {
	private final String url;
	private final long discordId;
	private final long guildId;

	public RandomUrlEntity(String url, long discordId, long guildId) {
		this.url = url;
		this.discordId = discordId;
		this.guildId = guildId;
	}

	public String getUrl() {
		return url;
	}

	public long getDiscordId() {
		return discordId;
	}

	public long getGuildId() {
		return guildId;
	}
}
