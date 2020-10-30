package dao.entities;

public class Rating {
    private final long discordId;
    private final Byte rating;
    private final boolean isSameGuild;

    public Rating(long discordId, Byte rating, boolean isSameGuild) {
        this.discordId = discordId;
        this.rating = rating;
        this.isSameGuild = isSameGuild;
    }

    public long getDiscordId() {
        return discordId;
    }

    public Byte getRating() {
        return rating;
    }

    public boolean isSameGuild() {
        return isSameGuild;
    }
}
