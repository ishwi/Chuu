package dao.entities;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomUrlEntity that = (RandomUrlEntity) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(discordId, that.discordId) &&
                Objects.equals(guildId, that.guildId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, discordId, guildId);
    }
}
