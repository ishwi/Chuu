package dao.entities;

public class RandomUrlEntity {
    private final String url;
    private final Long discordId;
    private final Long guildId;

    public RandomUrlEntity(String url, Long discordId, Long guildId) {
        this.url = url;
        this.discordId = discordId;
        this.guildId = guildId;
    }

    public String getUrl() {
        return url;
    }

    public Long getDiscordId() {
        return discordId;
    }

    public Long getGuildId() {
        return guildId;
    }
}
