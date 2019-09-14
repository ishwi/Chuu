package dao.entities;

public class RandomUrlEntity {
	private final String url;
	private final long discordId;
	private final Long guildId;

	public RandomUrlEntity(String url, long discordId, Long guildId) {
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

	public Long getGuildId() {
		return guildId;
	}
}
