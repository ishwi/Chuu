package dao.entities;

public class ArtistUserPlays extends ArtistPlays {
    private final long discordId;

    public ArtistUserPlays(String name, int count_a, long discordId) {
        super(name, count_a);
        this.discordId = discordId;
    }

    public long getDiscordId() {
        return discordId;
    }

}
